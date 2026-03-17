package com.example.receiptscanner.ui.screens

import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.receiptscanner.data.ReceiptDao
import com.example.receiptscanner.data.ReceiptEntity
import com.example.receiptscanner.data.SupabaseSyncService
import com.example.receiptscanner.ui.components.GlassCard
import com.example.receiptscanner.ui.components.GlassTopAppBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptListScreen(
    dao: ReceiptDao,
    onBack: () -> Unit,
    onOpen: (ReceiptEntity) -> Unit,
    onSettings: () -> Unit,
    onSyncError: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val syncService = remember { SupabaseSyncService(context) }
    val receipts by dao.getAll().collectAsState(initial = null)
    var isSyncing by remember { mutableStateOf(false) }
    val prefs = remember { context.getSharedPreferences("receipt_list_prefs", Context.MODE_PRIVATE) }
    var isGridView by remember { mutableStateOf(prefs.getBoolean("is_grid_view", false)) }

    Scaffold(
        topBar = {
            GlassTopAppBar(
                title = { Text("My Receipts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isGridView = !isGridView
                        prefs.edit().putBoolean("is_grid_view", isGridView).apply()
                    }) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = if (isGridView) "List View" else "Grid View"
                        )
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
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
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            receipts == null -> {
                SkeletonReceiptList(
                    isGridView = isGridView,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(padding)
                )
            }
            receipts!!.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    GlassCard(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "No receipts yet. Start scanning!",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            else -> {
                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .padding(padding)
                            .padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(receipts!!) { receipt ->
                            GridReceiptItem(receipt = receipt, onOpen = onOpen)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .padding(padding),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(receipts!!) { receipt ->
                            GlassCard(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .fillMaxWidth()
                                    .clickable { onOpen(receipt) }
                            ) {
                                ListItem(
                                    headlineContent = {
                                        Text(receipt.merchant + if (receipt.synced) " ✓" else "")
                                    },
                                    supportingContent = { Text("Total: ${receipt.total ?: "-"}") },
                                    colors = ListItemDefaults.colors(
                                        containerColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GridReceiptItem(receipt: ReceiptEntity, onOpen: (ReceiptEntity) -> Unit) {
    GlassCard(modifier = Modifier.clickable { onOpen(receipt) }) {
        Column(modifier = Modifier.padding(8.dp)) {
            val imageModel = when {
                receipt.imagePath.isNotBlank() && File(receipt.imagePath).exists() -> File(receipt.imagePath)
                !receipt.imageUrl.isNullOrBlank() -> receipt.imageUrl
                else -> null
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.Gray.copy(alpha = 0.2f))
            ) {
                if (imageModel != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageModel),
                        contentDescription = "Receipt image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ViewList,
                        contentDescription = "No image",
                        modifier = Modifier.align(Alignment.Center),
                        tint = Color.Gray
                    )
                }
            }
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = receipt.merchant + if (receipt.synced) " ✓" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = receipt.total ?: "-",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    )
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 300f, 0f),
        end = Offset(translateAnim, 0f)
    )
}

@Composable
private fun SkeletonReceiptList(isGridView: Boolean, modifier: Modifier = Modifier) {
    val shimmer = ShimmerBrush()
    if (isGridView) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier.padding(horizontal = 8.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(6) {
                GlassCard { SkeletonGridItem(shimmer) }
            }
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(6) {
                GlassCard(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                ) {
                    SkeletonListItem(shimmer)
                }
            }
        }
    }
}

@Composable
private fun SkeletonListItem(shimmer: Brush) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(16.dp)
                .background(shimmer, RoundedCornerShape(4.dp))
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.35f)
                .height(12.dp)
                .background(shimmer, RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun SkeletonGridItem(shimmer: Brush) {
    Column(modifier = Modifier.padding(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(shimmer, MaterialTheme.shapes.medium)
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(14.dp)
                .background(shimmer, RoundedCornerShape(4.dp))
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.45f)
                .height(12.dp)
                .background(shimmer, RoundedCornerShape(4.dp))
        )
    }
}
