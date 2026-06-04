package com.example.receiptscanner.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.receiptscanner.data.PaymentMethods
import com.example.receiptscanner.data.ReceiptTags
import com.example.receiptscanner.ui.components.GlassCard
import com.example.receiptscanner.ui.components.GlassTopAppBar
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ResultScreen(
    imagePath: String,
    isSaving: Boolean,
    onSave: (tags: Set<String>, paymentMethod: String?) -> Unit
) {
    var selectedTags by remember { mutableStateOf(emptySet<String>()) }
    var selectedPaymentMethod by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            GlassTopAppBar(title = { Text("Confirm Receipt") })
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Image(
                painter = rememberAsyncImagePainter(File(imagePath)),
                contentDescription = "Receipt preview",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tags",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReceiptTags.all.forEach { tag ->
                                FilterChip(
                                    selected = tag in selectedTags,
                                    onClick = {
                                        selectedTags = if (tag in selectedTags) {
                                            selectedTags - tag
                                        } else {
                                            selectedTags + tag
                                        }
                                    },
                                    label = { Text(tag) },
                                    enabled = !isSaving
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Payment method",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PaymentMethods.all.forEach { method ->
                                FilterChip(
                                    selected = selectedPaymentMethod == method,
                                    onClick = {
                                        selectedPaymentMethod =
                                            if (selectedPaymentMethod == method) null else method
                                    },
                                    label = { Text(method) },
                                    enabled = !isSaving
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (isSaving) {
                    Text(
                        text = "Saving and uploading…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = { onSave(selectedTags, selectedPaymentMethod) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Save Receipt")
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
