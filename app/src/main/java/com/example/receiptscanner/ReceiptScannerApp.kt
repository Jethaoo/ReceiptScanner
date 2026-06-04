package com.example.receiptscanner

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import com.example.receiptscanner.data.AppDatabase
import com.example.receiptscanner.data.ReceiptDao
import com.example.receiptscanner.data.ReceiptEntity
import com.example.receiptscanner.data.toTagsStorage
import com.example.receiptscanner.data.toTagList
import com.example.receiptscanner.data.SupabaseSyncService
import com.example.receiptscanner.navigation.MainTab
import com.example.receiptscanner.navigation.Screen
import com.example.receiptscanner.ui.components.BarItem
import com.example.receiptscanner.ui.components.MainBottomNavBar
import com.example.receiptscanner.ui.screens.CameraScreen
import com.example.receiptscanner.ui.screens.EditReceiptScreen
import com.example.receiptscanner.ui.screens.HomeScreen
import com.example.receiptscanner.ui.screens.PreviewScreen
import com.example.receiptscanner.ui.screens.ReceiptDetailScreen
import com.example.receiptscanner.ui.screens.ReceiptListScreen
import com.example.receiptscanner.ui.screens.ResultScreen
import com.example.receiptscanner.ui.screens.ScanScreen
import com.example.receiptscanner.ui.screens.SettingsScreen
import com.example.receiptscanner.utils.*
import com.example.receiptscanner.utils.getLastNMonthRanges
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PREFS_RECEIPT_SCANNER = "receipt_scanner_prefs"
private const val PREF_AUTO_SAVE_GALLERY = "auto_save_gallery"

@Composable
fun ReceiptScannerApp(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.get(context) }
    val dao = remember { db.receiptDao() }
    val syncService = remember { SupabaseSyncService(context) }
    val appPrefs = remember {
        context.getSharedPreferences(PREFS_RECEIPT_SCANNER, Context.MODE_PRIVATE)
    }
    var autoSaveGalleryAfterUpload by remember {
        mutableStateOf(appPrefs.getBoolean(PREF_AUTO_SAVE_GALLERY, false))
    }

    var selectedTab by remember { mutableStateOf(MainTab.Home) }
    var flowStack by remember { mutableStateOf<List<Screen>>(emptyList()) }

    val receiptCount by dao.count().collectAsState(initial = 0)
    val monthOptions = remember { getLastNMonthRanges(6) }
    var selectedMonthIndex by remember { mutableStateOf(monthOptions.lastIndex.coerceAtLeast(0)) }

    val rangeStart = monthOptions.first().startAtMillis
    val rangeEnd = monthOptions.last().endAtMillis
    val receiptsInRange by dao.getReceiptsBetween(rangeStart, rangeEnd).collectAsState(initial = emptyList())

    val monthlyTrendBars = remember(receiptsInRange) {
        monthOptions.map { month ->
            val count = receiptsInRange.count { it.createdAt in month.startAtMillis..month.endAtMillis }
            BarItem(label = month.label, value = count)
        }
    }

    val selectedMonth = monthOptions.getOrNull(selectedMonthIndex)

    val tagBars = remember(receiptsInRange, selectedMonthIndex, selectedMonth) {
        if (selectedMonth == null) return@remember emptyList()
        val selectedReceipts = receiptsInRange.filter { it.createdAt in selectedMonth.startAtMillis..selectedMonth.endAtMillis }
        val counts = mutableMapOf<String, Int>()
        selectedReceipts.forEach { receipt ->
            receipt.tags.toTagList().forEach { tag ->
                if (tag.isBlank()) return@forEach
                counts[tag] = (counts[tag] ?: 0) + 1
            }
        }
        counts.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { BarItem(label = it.key, value = it.value) }
    }

    val paymentBars = remember(receiptsInRange, selectedMonthIndex, selectedMonth) {
        if (selectedMonth == null) return@remember emptyList()
        val selectedReceipts = receiptsInRange.filter { it.createdAt in selectedMonth.startAtMillis..selectedMonth.endAtMillis }
        val counts = mutableMapOf<String, Int>()
        selectedReceipts.forEach { receipt ->
            val method = receipt.paymentMethod?.takeIf { it.isNotBlank() } ?: "Unspecified"
            counts[method] = (counts[method] ?: 0) + 1
        }
        counts.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { BarItem(label = it.key, value = it.value) }
    }
    var isLoading by remember { mutableStateOf(false) }
    var isSavingReceipt by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    fun navigateTo(screen: Screen) {
        flowStack = flowStack + screen
    }

    fun navigateBack() {
        if (flowStack.isEmpty()) return
        flowStack = flowStack.dropLast(1)
    }

    fun onTabSelected(tab: MainTab) {
        selectedTab = tab
        flowStack = emptyList()
    }

    fun handlePreviewRetry(imagePath: String) {
        deleteReceiptImage(context, imagePath)
        navigateBack()
    }

    fun onEditSaved(updated: ReceiptEntity) {
        scope.launch {
            try {
                val updatedReceipt = updated.copy(synced = false)
                dao.update(updatedReceipt)
                flowStack = flowStack.dropLast(2) + Screen.Detail(updatedReceipt)
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Failed to update receipt: ${e.message ?: "Unknown error"}"
            }
        }
    }

    val currentFlowScreen = flowStack.lastOrNull()

    BackHandler(enabled = flowStack.isNotEmpty() || selectedTab != MainTab.Home) {
        when {
            flowStack.isNotEmpty() -> navigateBack()
            selectedTab != MainTab.Home -> selectedTab = MainTab.Home
        }
    }

    if (currentFlowScreen != null) {
        if (currentFlowScreen is Screen.Preview) {
            BackHandler {
                handlePreviewRetry(currentFlowScreen.imagePath)
            }
        }
        if (currentFlowScreen is Screen.Result) {
            BackHandler(enabled = !isSavingReceipt) {
                navigateBack()
            }
        }

        RenderFlowScreen(
            screen = currentFlowScreen,
            context = context,
            scope = scope,
            dao = dao,
            syncService = syncService,
            autoSaveGalleryAfterUpload = autoSaveGalleryAfterUpload,
            isLoading = isLoading,
            onLoadingChange = { isLoading = it },
            isSavingReceipt = isSavingReceipt,
            onSavingReceiptChange = { isSavingReceipt = it },
            onError = { errorMessage = it },
            onSuccess = { successMessage = it },
            navigateTo = ::navigateTo,
            navigateBack = ::navigateBack,
            onPreviewRetry = ::handlePreviewRetry,
            onEditSaved = ::onEditSaved,
            onFlowCompleteToHome = {
                flowStack = emptyList()
                selectedTab = MainTab.Home
            }
        )
    } else {
        Scaffold(
            bottomBar = {
                MainBottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = ::onTabSelected
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (selectedTab) {
                    MainTab.Home -> HomeScreen(
                        receiptCount = receiptCount,
                        monthOptions = monthOptions,
                        selectedMonthIndex = selectedMonthIndex,
                        onSelectMonth = { selectedMonthIndex = it },
                        monthlyTrendBars = monthlyTrendBars,
                        tagBars = tagBars,
                        paymentBars = paymentBars
                    )
                    MainTab.Scan -> ScanScreen(
                        onCapture = { navigateTo(Screen.Camera) },
                        onImagePicked = { path -> navigateTo(Screen.Preview(path)) },
                        onError = { errorMessage = it }
                    )
                    MainTab.Receipts -> ReceiptListScreen(
                        dao = dao,
                        onOpen = { navigateTo(Screen.Detail(it)) },
                        onUserMessage = { message, isError ->
                            if (isError) errorMessage = message else successMessage = message
                        },
                        isTabRoot = true
                    )
                    MainTab.Settings -> SettingsScreen(
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        autoSaveGalleryAfterUpload = autoSaveGalleryAfterUpload,
                        onAutoSaveGalleryAfterUploadChange = { enabled ->
                            autoSaveGalleryAfterUpload = enabled
                            appPrefs.edit().putBoolean(PREF_AUTO_SAVE_GALLERY, enabled).apply()
                        },
                        onRestoreReceipts = {
                            scope.launch {
                                isLoading = true
                                val result = syncService.restoreReceipts(dao)
                                result.onSuccess { count ->
                                    successMessage =
                                        "Successfully restored $count receipt(s) from cloud."
                                }.onFailure { e ->
                                    errorMessage = "Failed to restore receipts: ${e.message}"
                                }
                                isLoading = false
                            }
                        },
                        isTabRoot = true
                    )
                }
            }
        }
    }

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

@Composable
private fun RenderFlowScreen(
    screen: Screen,
    context: Context,
    scope: CoroutineScope,
    dao: ReceiptDao,
    syncService: SupabaseSyncService,
    autoSaveGalleryAfterUpload: Boolean,
    isLoading: Boolean,
    onLoadingChange: (Boolean) -> Unit,
    isSavingReceipt: Boolean,
    onSavingReceiptChange: (Boolean) -> Unit,
    onError: (String) -> Unit,
    onSuccess: (String) -> Unit,
    navigateTo: (Screen) -> Unit,
    navigateBack: () -> Unit,
    onPreviewRetry: (String) -> Unit,
    onEditSaved: (ReceiptEntity) -> Unit,
    onFlowCompleteToHome: () -> Unit
) {
    when (val s = screen) {
        Screen.Camera -> CameraScreen(
            onBack = navigateBack,
            onImage = { imagePath -> navigateTo(Screen.Preview(imagePath)) },
            onError = onError
        )

        is Screen.Preview -> PreviewScreen(
            imagePath = s.imagePath,
            onUse = {
                onLoadingChange(true)
                scope.launch {
                    runOcr(context, s.imagePath)
                        .onSuccess { text ->
                            onLoadingChange(false)
                            navigateTo(Screen.Result(s.imagePath, text))
                        }
                        .onFailure { exception ->
                            onLoadingChange(false)
                            onError("OCR failed: ${exception.message}")
                        }
                }
            },
            onRetake = { onPreviewRetry(s.imagePath) },
            isLoading = isLoading
        )

        is Screen.Result -> ResultScreen(
            imagePath = s.imagePath,
            isSaving = isSavingReceipt,
            onSave = { tags, paymentMethod ->
                scope.launch {
                    onSavingReceiptChange(true)
                    try {
                        val receipt = ReceiptEntity(
                            id = System.currentTimeMillis().toString(),
                            merchant = s.text.lineSequence().firstOrNull() ?: "Unknown",
                            date = extractDate(s.text),
                            total = extractTotal(s.text),
                            imagePath = s.imagePath,
                            createdAt = System.currentTimeMillis(),
                            synced = false,
                            imageUrl = null,
                            tags = tags.toTagsStorage(),
                            paymentMethod = paymentMethod
                        )
                        dao.insert(receipt)
                        syncService.syncReceipt(receipt)
                            .onSuccess { synced ->
                                dao.update(synced)
                                if (autoSaveGalleryAfterUpload) {
                                    val galleryResult = withContext(Dispatchers.IO) {
                                        val f = File(s.imagePath)
                                        if (!f.exists()) {
                                            Result.failure(Exception("Local image not found"))
                                        } else {
                                            saveReceiptJpegToGallery(
                                                context,
                                                f.readBytes(),
                                                "Receipt_${synced.id}.jpg"
                                            )
                                        }
                                    }
                                    onSuccess(
                                        buildString {
                                            append("Receipt saved and uploaded successfully.")
                                            if (galleryResult.isSuccess) {
                                                append(" Image saved to Pictures/ReceiptScanner.")
                                            } else {
                                                append(" Could not save to gallery: ")
                                                append(
                                                    galleryResult.exceptionOrNull()?.message
                                                        ?: "unknown error"
                                                )
                                                append(".")
                                            }
                                        }
                                    )
                                } else {
                                    onSuccess("Receipt saved and uploaded successfully.")
                                }
                            }
                            .onFailure { e ->
                                onError(
                                    "Receipt saved locally. Cloud sync failed: ${e.message ?: "Unknown error"}"
                                )
                            }
                        onFlowCompleteToHome()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onError("Failed to save receipt: ${e.message ?: "Unknown error"}")
                    } finally {
                        onSavingReceiptChange(false)
                    }
                }
            }
        )

        is Screen.Detail -> ReceiptDetailScreen(
            receipt = s.receipt,
            onBack = navigateBack,
            onDelete = {
                scope.launch {
                    try {
                        deleteReceiptImage(context, s.receipt.imagePath)
                        dao.delete(s.receipt.id)
                        navigateBack()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onError("Failed to delete receipt: ${e.message ?: "Unknown error"}")
                    }
                }
            },
            onEdit = { navigateTo(Screen.Edit(s.receipt)) }
        )

        is Screen.Edit -> EditReceiptScreen(
            receipt = s.receipt,
            onCancel = navigateBack,
            onSave = onEditSaved
        )

        Screen.Home,
        Screen.List,
        Screen.Settings -> { /* tab roots only */ }
    }
}
