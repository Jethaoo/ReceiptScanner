package com.example.receiptscanner.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.receiptscanner.ui.components.GlassCard
import com.example.receiptscanner.ui.components.GlassTopAppBar
import com.example.receiptscanner.utils.copyReceiptImageFromUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onCapture: () -> Unit,
    onImagePicked: (String) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        copyReceiptImageFromUri(context, uri)
            .onSuccess(onImagePicked)
            .onFailure { e ->
                onError(e.message ?: "Could not use selected image.")
            }
    }

    Scaffold(
        topBar = {
            GlassTopAppBar(
                title = { Text("Scan") }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            GlassCard(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onCapture,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Capture receipt")
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            pickImageLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Upload receipt")
                    }
                }
            }
        }
    }
}
