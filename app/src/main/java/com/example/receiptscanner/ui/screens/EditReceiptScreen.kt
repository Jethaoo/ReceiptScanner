package com.example.receiptscanner.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.receiptscanner.data.ReceiptEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReceiptScreen(
    receipt: ReceiptEntity,
    onSave: (ReceiptEntity) -> Unit,
    onCancel: () -> Unit
) {
    var merchant by remember { mutableStateOf(receipt.merchant) }
    var date by remember { mutableStateOf(receipt.date ?: "") }
    var total by remember { mutableStateOf(receipt.total ?: "") }

    Scaffold(topBar = { TopAppBar(title = { Text("Edit Receipt") }) }) {
        Column(Modifier.padding(it).padding(16.dp)) {
            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it },
                label = { Text("Merchant") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = total,
                onValueChange = { total = it },
                label = { Text("Total") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onSave(receipt.copy(merchant = merchant, date = date, total = total)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}

