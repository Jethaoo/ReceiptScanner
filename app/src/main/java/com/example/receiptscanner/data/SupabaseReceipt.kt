package com.example.receiptscanner.data

import kotlinx.serialization.Serializable

/**
 * Supabase receipt data model
 * This matches the table structure in Supabase
 */
@Serializable
data class SupabaseReceipt(
    val id: String,
    val merchant: String,
    val date: String? = null,
    val total: String? = null,
    val image_url: String? = null,
    val created_at: Long,
    val updated_at: Long = System.currentTimeMillis()
)

/**
 * Convert ReceiptEntity to SupabaseReceipt
 */
fun ReceiptEntity.toSupabaseReceipt(): SupabaseReceipt {
    return SupabaseReceipt(
        id = id,
        merchant = merchant,
        date = date,
        total = total,
        image_url = imageUrl,
        created_at = createdAt,
        updated_at = System.currentTimeMillis()
    )
}

/**
 * Convert SupabaseReceipt to ReceiptEntity
 */
fun SupabaseReceipt.toReceiptEntity(imagePath: String): ReceiptEntity {
    return ReceiptEntity(
        id = id,
        merchant = merchant,
        date = date,
        total = total,
        imagePath = imagePath,
        createdAt = created_at,
        synced = true,
        imageUrl = image_url
    )
}

