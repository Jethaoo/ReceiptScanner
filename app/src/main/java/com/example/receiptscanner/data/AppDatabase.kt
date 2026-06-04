package com.example.receiptscanner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ReceiptEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun receiptDao(): ReceiptDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE receipts ADD COLUMN synced INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) { }
                try {
                    db.execSQL("ALTER TABLE receipts ADD COLUMN imageUrl TEXT")
                } catch (_: Exception) { }
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE receipts ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) { }
                try {
                    db.execSQL("ALTER TABLE receipts ADD COLUMN paymentMethod TEXT")
                } catch (_: Exception) { }
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "receipts.db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build().also { INSTANCE = it }
            }
    }
}
