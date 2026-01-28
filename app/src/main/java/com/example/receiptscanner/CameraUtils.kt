package com.example.receiptscanner

import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { cont ->
        ProcessCameraProvider.getInstance(this).also {
            it.addListener(
                { cont.resume(it.get()) },
                ContextCompat.getMainExecutor(this)
            )
        }
    }
