package com.example.receiptscanner.ui.screens

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import android.util.Size
import java.io.File
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.receiptscanner.getCameraProvider
import com.example.receiptscanner.ui.components.GlassCard
import com.example.receiptscanner.ui.components.GlassTopAppBar
import com.example.receiptscanner.utils.rememberCameraPermissionState
import com.example.receiptscanner.utils.takePhoto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onBack: () -> Unit,
    onImage: (String) -> Unit,
    onError: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember {
        val resolutionSelector = ResolutionSelector.Builder()
            .setResolutionStrategy(
                ResolutionStrategy(
                    Size(1920, 1080),
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                )
            )
            .build()
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setResolutionSelector(resolutionSelector)
            .setJpegQuality(88)
            .setFlashMode(ImageCapture.FLASH_MODE_OFF)
            .build()
    }

    var isCapturing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val (hasPermission, requestPermission) = rememberCameraPermissionState(
        onPermissionGranted = {
            errorMessage = null
        },
        onPermissionDenied = {
            errorMessage = "Camera permission is required to scan receipts"
            onError?.invoke("Camera permission denied")
        }
    )

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            requestPermission()
        } else {
            File(context.filesDir, "receipt_images").mkdirs()
        }
    }

    Scaffold(
        topBar = {
            GlassTopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!hasPermission) {
                        requestPermission()
                        return@FloatingActionButton
                    }
                    if (isCapturing) return@FloatingActionButton
                    isCapturing = true
                    takePhoto(
                        context = context,
                        imageCapture = imageCapture,
                        onDone = { path ->
                            isCapturing = false
                            onImage(path)
                        },
                        onError = { exception ->
                            isCapturing = false
                            val error = "Failed to capture photo: ${exception.message}"
                            errorMessage = error
                            onError?.invoke(error)
                        }
                    )
                },
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .size(72.dp)
                    .border(4.dp, androidx.compose.ui.graphics.Color.White, CircleShape),
                shape = CircleShape,
                containerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                contentColor = androidx.compose.ui.graphics.Color.Transparent
            ) {
                // Empty content for a simple shutter button look
            }
        },
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.Center,
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(modifier = Modifier.padding(24.dp)) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(errorMessage ?: "Unknown error")
                        Button(onClick = { requestPermission() }) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        } else if (hasPermission) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
            LaunchedEffect(Unit) {
                try {
                    val provider = context.getCameraProvider()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    val error = "Failed to initialize camera: ${e.message}"
                    errorMessage = error
                    onError?.invoke(error)
                }
            }
        }
    }
}

