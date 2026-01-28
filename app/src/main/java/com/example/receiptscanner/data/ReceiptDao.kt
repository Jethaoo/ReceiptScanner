package com.example.receiptscanner.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(receipt: ReceiptEntity)

    @Update
    suspend fun update(receipt: ReceiptEntity)

    @Query("DELETE FROM receipts WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM receipts ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ReceiptEntity>>
    
    @Query("SELECT * FROM receipts WHERE synced = 0 OR synced IS NULL")
    suspend fun getUnsynced(): List<ReceiptEntity>
}

