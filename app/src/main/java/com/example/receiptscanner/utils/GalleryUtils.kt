package com.example.receiptscanner.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.receiptscanner.data.ReceiptEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

private val albumRelativePath = "${Environment.DIRECTORY_PICTURES}/ReceiptScanner"

/**
 * Writes JPEG bytes to the shared Pictures/ReceiptScanner album (MediaStore).
 */
suspend fun saveReceiptJpegToGallery(
    context: Context,
    bytes: ByteArray,
    displayName: String
): Result<String> = withContext(Dispatchers.IO) {
    try {
        val resolver = context.contentResolver
        val name = if (displayName.endsWith(".jpg", ignoreCase = true)) displayName else "$displayName.jpg"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, albumRelativePath)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val uri = resolver.insert(collection, values)
            ?: return@withContext Result.failure(Exception("Could not create gallery entry"))
        resolver.openOutputStream(uri)?.use { it.write(bytes) }
            ?: return@withContext Result.failure(Exception("Could not write image"))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }
        Result.success(albumRelativePath)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

suspend fun receiptImageBytesOrNull(receipt: ReceiptEntity): ByteArray? = withContext(Dispatchers.IO) {
    val path = receipt.imagePath
    if (path.isNotBlank()) {
        val f = File(path)
        if (f.exists() && f.isFile) return@withContext f.readBytes()
    }
    val urlStr = receipt.imageUrl?.trim()?.takeIf { it.isNotEmpty() } ?: return@withContext null
    try {
        val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
            connectTimeout = 30_000
            readTimeout = 60_000
            instanceFollowRedirects = true
        }
        conn.inputStream.use { it.readBytes() }
    } catch (_: Exception) {
        null
    }
}

suspend fun saveReceiptToGallery(context: Context, receipt: ReceiptEntity): Result<String> {
    val bytes = receiptImageBytesOrNull(receipt)
        ?: return Result.failure(Exception("No image available"))
    val name = sanitizeFileName("Receipt_${receipt.id}.jpg")
    return saveReceiptJpegToGallery(context, bytes, name)
}

fun sanitizeFileName(name: String): String =
    name.replace(Regex("""[\\/:*?"<>|]"""), "_")

fun receiptHasDownloadableImage(receipt: ReceiptEntity): Boolean {
    if (receipt.imagePath.isNotBlank() && File(receipt.imagePath).exists()) return true
    return !receipt.imageUrl.isNullOrBlank()
}
