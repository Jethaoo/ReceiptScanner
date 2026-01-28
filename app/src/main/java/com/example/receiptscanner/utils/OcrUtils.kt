package com.example.receiptscanner.utils

import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/**
 * OCR utility functions with proper error handling
 */
fun runOcr(
    path: String,
    onResult: (String) -> Unit,
    onError: ((Exception) -> Unit)? = null
) {
    try {
        val bitmap = BitmapFactory.decodeFile(path) ?: run {
            onError?.invoke(Exception("Failed to decode image from path: $path"))
            return
        }
        val image = InputImage.fromBitmap(bitmap, 0)
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            .process(image)
            .addOnSuccessListener { result ->
                onResult(result.text)
            }
            .addOnFailureListener { exception ->
                onError?.invoke(exception)
            }
    } catch (e: Exception) {
        onError?.invoke(e)
    }
}

