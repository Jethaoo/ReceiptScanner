package com.example.receiptscanner.utils

import android.content.Context
import androidx.core.net.toUri
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Runs ML Kit Latin on-device OCR on a receipt image.
 *
 * Improvements over a naive decode + [InputImage.fromBitmap]:
 * - [InputImage.fromFilePath] applies JPEG EXIF orientation so text is not read sideways.
 * - Text is assembled in top-to-bottom, left-to-right order from blocks/lines (better for totals/dates).
 * - Recognition runs on [Dispatchers.IO] and the recognizer is closed after use.
 */
suspend fun runOcr(context: Context, imagePath: String): Result<String> = withContext(Dispatchers.IO) {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    try {
        val image = try {
            InputImage.fromFilePath(context, File(imagePath).toUri())
        } catch (e: IOException) {
            return@withContext Result.failure(Exception("Could not read image file.", e))
        }
        val visionText = Tasks.await(recognizer.process(image))
        Result.success(orderedTextFromVision(visionText))
    } catch (e: Exception) {
        Result.failure(e)
    } finally {
        recognizer.close()
    }
}

private fun orderedTextFromVision(visionText: Text): String {
    val blocks = visionText.textBlocks
    if (blocks.isNullOrEmpty()) return visionText.text
    return blocks
        .sortedWith(
            compareBy<Text.TextBlock> { it.boundingBox?.top ?: 0 }
                .thenBy { it.boundingBox?.left ?: 0 }
        )
        .joinToString("\n") { orderedLinesFromBlock(it) }
}

private fun orderedLinesFromBlock(block: Text.TextBlock): String {
    val lines = block.lines
    if (lines.isNullOrEmpty()) return block.text
    return lines
        .sortedWith(
            compareBy<Text.Line> { it.boundingBox?.top ?: 0 }
                .thenBy { it.boundingBox?.left ?: 0 }
        )
        .joinToString("\n") { it.text }
}
