package com.example.receiptscanner.data

import android.content.Context
import android.graphics.BitmapFactory
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.time.Duration.Companion.seconds

/**
 * Service for syncing receipts with Supabase
 */
class SupabaseSyncService(private val context: Context) {
    
    private val supabase: io.github.jan.supabase.SupabaseClient? by lazy {
        try {
            SupabaseClient.client
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null if Supabase initialization fails
        }
    }
    private val storageBucket = "receipt-images" // Create this bucket in Supabase Storage
    
    /**
     * Sync a single receipt to Supabase
     */
    suspend fun syncReceipt(receipt: ReceiptEntity): Result<ReceiptEntity> = withContext(Dispatchers.IO) {
        try {
            val client = supabase ?: return@withContext Result.failure(
                Exception("Supabase not configured. Check your API credentials.")
            )
            
            // Upload image to Supabase Storage if not already uploaded
            val imageUrl = if (receipt.imageUrl == null && receipt.imagePath.isNotEmpty()) {
                uploadImage(receipt.id, receipt.imagePath).getOrNull()
            } else {
                receipt.imageUrl
            }
            
            // Convert to Supabase format
            val supabaseReceipt = try {
                receipt.copy(imageUrl = imageUrl).toSupabaseReceipt()
            } catch (e: Exception) {
                return@withContext Result.failure(Exception("Failed to convert receipt: ${e.message}"))
            }
            
            // Upsert to Supabase (insert or update)
            try {
                client.from("receipts").upsert(supabaseReceipt)
            } catch (e: Exception) {
                return@withContext Result.failure(Exception("Failed to upload to Supabase: ${e.message}"))
            }
            
            // Return updated receipt with sync status
            Result.success(receipt.copy(synced = true, imageUrl = imageUrl))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Sync failed: ${e.message ?: e.javaClass.simpleName}"))
        }
    }
    
    /**
     * Upload receipt image to Supabase Storage
     */
    private suspend fun uploadImage(receiptId: String, imagePath: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val client = supabase ?: return@withContext Result.failure(Exception("Supabase client not initialized"))
            
            val file = File(imagePath)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("Image file not found: $imagePath"))
            }
            
            val fileName = "$receiptId.jpg"
            val bytes = file.readBytes()
            
            // Upload to storage
            client.storage.from(storageBucket).upload(
                path = fileName,
                data = bytes,
                upsert = true
            )
            
            // Get public URL
            val url = client.storage.from(storageBucket).createSignedUrl(
                path = fileName,
                expiresIn = 31536000.seconds // 1 year
            )
            
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete receipt from Supabase
     */
    suspend fun deleteReceipt(receiptId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val client = supabase ?: return@withContext Result.failure(Exception("Supabase client not initialized"))
            
            // Delete from database
            client.from("receipts").delete {
                filter {
                    eq("id", receiptId)
                }
            }
            
            // Delete image from storage
            try {
                client.storage.from(storageBucket).delete("$receiptId.jpg")
            } catch (e: Exception) {
                // Ignore storage deletion errors
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Fetch all receipts from Supabase
     */
    suspend fun fetchAllReceipts(): Result<List<SupabaseReceipt>> = withContext(Dispatchers.IO) {
        try {
            val client = supabase ?: return@withContext Result.failure(Exception("Supabase client not initialized"))
            
            val receipts = client.from("receipts")
                .select()
                .decodeList<SupabaseReceipt>()
            
            Result.success(receipts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if Supabase is available
     */
    suspend fun isSupabaseAvailable(): Boolean {
        return try {
            supabase != null
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Sync all unsynced receipts
     */
    suspend fun syncAllUnsynced(receiptDao: ReceiptDao): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // Check if Supabase is configured
            val client = supabase
            if (client == null) {
                return@withContext Result.failure(
                    Exception("Supabase not configured. Please check your API credentials in SupabaseClient.kt")
                )
            }
            
            // Get unsynced receipts
            val unsyncedReceipts = try {
                receiptDao.getUnsynced()
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext Result.failure(
                    Exception("Database error: Failed to fetch unsynced receipts. ${e.message}")
                )
            }
            
            if (unsyncedReceipts.isEmpty()) {
                return@withContext Result.success(0)
            }
            
            var successCount = 0
            var errorCount = 0
            val errors = mutableListOf<String>()
            
            unsyncedReceipts.forEach { receipt ->
                try {
                    val result = syncReceipt(receipt)
                    result.onSuccess { syncedReceipt ->
                        try {
                            receiptDao.update(syncedReceipt)
                            successCount++
                        } catch (e: Exception) {
                            errorCount++
                            errors.add("Failed to update receipt ${receipt.id}: ${e.message}")
                            e.printStackTrace()
                        }
                    }.onFailure { e ->
                        errorCount++
                        errors.add("Failed to sync ${receipt.merchant}: ${e.message}")
                        e.printStackTrace()
                    }
                } catch (e: Exception) {
                    errorCount++
                    errors.add("Error syncing ${receipt.merchant}: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            // Return result based on outcomes
            when {
                successCount > 0 && errorCount == 0 -> Result.success(successCount)
                successCount > 0 && errorCount > 0 -> Result.failure(
                    Exception("Synced $successCount receipt(s), but $errorCount failed. ${errors.firstOrNull()}")
                )
                else -> Result.failure(
                    Exception("Failed to sync all receipts. ${errors.firstOrNull() ?: "Unknown error"}")
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Sync error: ${e.message ?: e.javaClass.simpleName}"))
        }
    }
}

