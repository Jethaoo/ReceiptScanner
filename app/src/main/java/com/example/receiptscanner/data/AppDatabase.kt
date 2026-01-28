package com.example.receiptscanner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ReceiptEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun receiptDao(): ReceiptDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Check if columns already exist before adding
                try {
                    database.execSQL("ALTER TABLE receipts ADD COLUMN synced INTEGER NOT NULL DEFAULT 0")
                } catch (e: Exception) {
                    // Column might already exist, ignore
                }
                try {
                    database.execSQL("ALTER TABLE receipts ADD COLUMN imageUrl TEXT")
                } catch (e: Exception) {
                    // Column might already exist, ignore
                }
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "receipts.db"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration() // For development - removes data on migration failure
                .build().also { INSTANCE = it }
            }
    }
}
