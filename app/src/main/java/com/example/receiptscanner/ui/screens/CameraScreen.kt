package com.example.receiptscanner.ui.screens

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.receiptscanner.getCameraProvider
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
    val imageCapture = remember { ImageCapture.Builder().build() }
    
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
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (hasPermission) {
                        takePhoto(
                            context = context,
                            imageCapture = imageCapture,
                            onDone = onImage,
                            onError = { exception ->
                                val error = "Failed to capture photo: ${exception.message}"
                                errorMessage = error
                                onError?.invoke(error)
                            }
                        )
                    } else {
                        requestPermission()
                    }
                }
            ) {
                Text("Scan")
            }
        }
    ) { padding ->
        if (errorMessage != null) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(errorMessage ?: "Unknown error")
                    Button(onClick = { requestPermission() }) {
                        Text("Grant Permission")
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

