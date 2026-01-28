package com.example.receiptscanner.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.receiptscanner.data.ReceiptDao
import com.example.receiptscanner.data.ReceiptEntity
import com.example.receiptscanner.data.SupabaseSyncService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptListScreen(
    dao: ReceiptDao,
    onBack: () -> Unit,
    onOpen: (ReceiptEntity) -> Unit,
    onSyncError: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val syncService = remember { SupabaseSyncService(context) }
    val receipts by dao.getAll().collectAsState(initial = emptyList())
    var isSyncing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Receipts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch(Dispatchers.Main) {
                                try {
                                    isSyncing = true
                                    val result = withContext(Dispatchers.IO) {
                                        syncService.syncAllUnsynced(dao)
                                    }
                                    result.onSuccess { count ->
                                        onSyncError?.invoke(
                                            if (count > 0) "Synced $count receipt(s)" else "No receipts to sync"
                                        )
                                    }.onFailure { e ->
                                        onSyncError?.invoke("Sync failed: ${e.message ?: "Unknown error"}")
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    onSyncError?.invoke("Sync error: ${e.message ?: e.javaClass.simpleName}")
                                } finally {
                                    isSyncing = false
                                }
                            }
                        },
                        enabled = !isSyncing
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Sync")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (receipts.isEmpty()) {
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    Text("No receipts yet. Start scanning!")
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(receipts) { receipt ->
                    ListItem(
                        headlineContent = { 
                            Text(receipt.merchant + if (receipt.synced) " ✓" else "")
                        },
                        supportingContent = { Text("Total: ${receipt.total ?: "-"}") },
                        modifier = Modifier.clickable { onOpen(receipt) }
                    )
                    Divider()
                }
            }
        }
    }
}

