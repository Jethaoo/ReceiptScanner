package com.example.receiptscanner.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.receiptscanner.data.ReceiptEntity
import com.example.receiptscanner.ui.components.GlassCard
import com.example.receiptscanner.ui.components.GlassTopAppBar
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDetailScreen(
    receipt: ReceiptEntity,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Scaffold(
        topBar = {
            GlassTopAppBar(
                title = { Text("Receipt") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp)
        ) {
            val imageModel = when {
                receipt.imagePath.isNotBlank() && File(receipt.imagePath).exists() -> File(receipt.imagePath)
                !receipt.imageUrl.isNullOrBlank() -> receipt.imageUrl
                else -> null
            }
            val showFullImage = remember { mutableStateOf(false) }
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = rememberAsyncImagePainter(imageModel),
                    contentDescription = "Receipt image for ${receipt.merchant}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clickable(enabled = imageModel != null) { showFullImage.value = true },
                    contentScale = ContentScale.Crop
                )
            }
            if (showFullImage.value && imageModel != null) {
                Dialog(
                    onDismissRequest = { showFullImage.value = false },
                ) {
                    val scale = remember { mutableStateOf(1f) }
                    val offset = remember { mutableStateOf(Offset.Zero) }
                    val state = rememberTransformableState { zoomChange, panChange, _ ->
                        val newScale = (scale.value * zoomChange).coerceIn(1f, 5f)
                        scale.value = newScale
                        offset.value += panChange
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { showFullImage.value = false })
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(imageModel),
                            contentDescription = "Receipt image for ${receipt.merchant}",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale.value,
                                    scaleY = scale.value,
                                    translationX = offset.value.x,
                                    translationY = offset.value.y
                                )
                                .transformable(state),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Merchant: ${receipt.merchant}")
                    Text("Date: ${receipt.date ?: "-"}")
                    Text("Total: ${receipt.total ?: "-"}")
                }
            }
            Spacer(Modifier.height(24.dp))
            androidx.compose.material3.Button(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit")
            }
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete")
            }
        }
    }
}

