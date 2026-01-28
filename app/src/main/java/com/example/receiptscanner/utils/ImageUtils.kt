package com.example.receiptscanner.utils

import android.content.Context
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import java.io.File

/**
 * Image capture and storage utilities.
 * Uses getFilesDir() instead of cacheDir for persistent storage.
 */
fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onDone: (String) -> Unit,
    onError: ((ImageCaptureException) -> Unit)? = null
) {
    // Use filesDir instead of cacheDir for persistent storage
    val imagesDir = File(context.filesDir, "receipt_images")
    if (!imagesDir.exists()) {
        imagesDir.mkdirs()
    }
    
    val file = File(imagesDir, "r_${System.currentTimeMillis()}.jpg")
    val options = ImageCapture.OutputFileOptions.Builder(file).build()
    imageCapture.takePicture(
        options,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(out: ImageCapture.OutputFileResults) {
                onDone(file.absolutePath)
            }
            override fun onError(e: ImageCaptureException) {
                onError?.invoke(e) ?: e.printStackTrace()
            }
        }
    )
}

/**
 * Deletes a receipt image file
 */
fun deleteReceiptImage(context: Context, imagePath: String) {
    try {
        File(imagePath).delete()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

