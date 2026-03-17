package com.example.receiptscanner

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.receiptscanner.data.AppDatabase
import com.example.receiptscanner.data.ReceiptEntity
import com.example.receiptscanner.data.SupabaseSyncService
import com.example.receiptscanner.navigation.Screen
import com.example.receiptscanner.ui.screens.CameraScreen
import com.example.receiptscanner.ui.screens.EditReceiptScreen
import com.example.receiptscanner.ui.screens.HomeScreen
import com.example.receiptscanner.ui.screens.PreviewScreen
import com.example.receiptscanner.ui.screens.ReceiptDetailScreen
import com.example.receiptscanner.ui.screens.ReceiptListScreen
import com.example.receiptscanner.ui.screens.ResultScreen
import com.example.receiptscanner.ui.screens.SettingsScreen
import com.example.receiptscanner.utils.*
import kotlinx.coroutines.launch

@Composable
fun ReceiptScannerApp(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.get(context) }
    val syncService = remember { SupabaseSyncService(context) }
    
    // Navigation back stack
    val navigationStack = remember { mutableStateOf(listOf<Screen>(Screen.Home)) }
    val currentScreen = navigationStack.value.lastOrNull() ?: Screen.Home
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isSyncing by remember { mutableStateOf(false) }
    
    // Function to navigate to a new screen
    fun navigateTo(screen: Screen) {
        navigationStack.value = navigationStack.value + screen
    }
    
    // Function to navigate back
    fun navigateBack() {
        if (navigationStack.value.size > 1) {
            navigationStack.value = navigationStack.value.dropLast(1)
        }
    }
    
    // Handle back button press
    BackHandler(enabled = currentScreen != Screen.Home) {
        navigateBack()
    }

    when (val s = currentScreen) {
        Screen.Home -> HomeScreen(
            onScan = { navigateTo(Screen.Camera) },
            onView = { navigateTo(Screen.List) },
            onSettings = { navigateTo(Screen.Settings) }
        )

        Screen.Camera -> CameraScreen(
            onBack = { navigateBack() },
            onImage = { imagePath ->
                navigateTo(Screen.Preview(imagePath))
            },
            onError = { error ->
                errorMessage = error
            }
        )

        is Screen.Preview -> {
            PreviewScreen(
                imagePath = s.imagePath,
                onUse = {
                    isLoading = true
                    runOcr(
                        path = s.imagePath,
                        onResult = { text ->
                            isLoading = false
                            navigateTo(Screen.Result(s.imagePath, text))
                        },
                        onError = { exception ->
                            isLoading = false
                            errorMessage = "OCR failed: ${exception.message}"
                        }
                    )
                },
                onRetake = {
                    deleteReceiptImage(context, s.imagePath)
                    navigateBack()
                },
                isLoading = isLoading
            )
            // Handle back button for preview screen
            BackHandler {
                deleteReceiptImage(context, s.imagePath)
                navigateBack()
            }
        }

        is Screen.Result -> {
            ResultScreen(
                imagePath = s.imagePath,
                text = s.text,
                onSave = {
                    scope.launch {
                        try {
                            val receipt = ReceiptEntity(
                                id = System.currentTimeMillis().toString(),
                                merchant = s.text.lineSequence().firstOrNull() ?: "Unknown",
                                date = extractDate(s.text),
                                total = extractTotal(s.text),
                                imagePath = s.imagePath,
                                createdAt = System.currentTimeMillis(),
                                synced = false,
                                imageUrl = null
                            )
                            db.receiptDao().insert(receipt)
                            
                            // Receipt saved locally - sync is optional (user can sync manually)
                            
                            // Clear navigation stack and go to home
                            navigationStack.value = listOf(Screen.Home)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            errorMessage = "Failed to save receipt: ${e.message ?: "Unknown error"}"
                        }
                    }
                }
            )
            // Handle back button for result screen - go back to preview
            BackHandler {
                navigateBack()
            }
        }

        Screen.List -> ReceiptListScreen(
            dao = db.receiptDao(),
            onBack = { navigateBack() },
            onOpen = { navigateTo(Screen.Detail(it)) },
            onSettings = { navigateTo(Screen.Settings) },
            onSyncError = { message ->
                if (message.startsWith("Synced") || message.startsWith("No receipts")) {
                    successMessage = message
                } else {
                    errorMessage = message
                }
            }
        )

        Screen.Settings -> SettingsScreen(
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            onBack = { navigateBack() }
        )

        is Screen.Detail -> ReceiptDetailScreen(
            receipt = s.receipt,
            onBack = { navigateBack() },
            onDelete = {
                scope.launch {
                    try {
                        deleteReceiptImage(context, s.receipt.imagePath)
                        db.receiptDao().delete(s.receipt.id)
                        
                        // Receipt deleted locally - sync deletion is optional (user can sync manually)
                        // Note: If receipt was synced, it will remain in Supabase until manual sync
                        
                        navigateBack()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorMessage = "Failed to delete receipt: ${e.message ?: "Unknown error"}"
                    }
                }
            },
            onEdit = { navigateTo(Screen.Edit(s.receipt)) }
        )

        is Screen.Edit -> EditReceiptScreen(
            receipt = s.receipt,
            onCancel = { navigateBack() },
            onSave = {
                scope.launch {
                    try {
                        val updatedReceipt = it.copy(synced = false)
                        db.receiptDao().update(updatedReceipt)
                        
                        // Receipt updated locally - sync is optional (user can sync manually)
                        
                        // Go back to detail screen
                        navigateBack()
                        // Update the detail screen with new data
                        val updatedStack = navigationStack.value.dropLast(1)
                        navigationStack.value = updatedStack + Screen.Detail(updatedReceipt)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorMessage = "Failed to update receipt: ${e.message ?: "Unknown error"}"
                    }
                }
            }
        )
    }

    // Show success dialog
    successMessage?.let { message ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { successMessage = null },
            title = { androidx.compose.material3.Text("Success") },
            text = { androidx.compose.material3.Text(message) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { successMessage = null }) {
                    androidx.compose.material3.Text("OK")
                }
            }
        )
    }
    
    // Show error dialog if there's an error
    errorMessage?.let { error ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { androidx.compose.material3.Text("Error") },
            text = { androidx.compose.material3.Text(error) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { errorMessage = null }) {
                    androidx.compose.material3.Text("OK")
                }
            }
        )
    }
}

