package com.example.receiptscanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey val id: String,
    val merchant: String,
    val date: String?,
    val total: String?,
    val imagePath: String,
    val createdAt: Long,
    val synced: Boolean = false,
    val imageUrl: String? = null // Supabase Storage URL
)
