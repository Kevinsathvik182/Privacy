package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.MessageDao
import com.example.data.local.dao.PeerDao
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.PeerEntity

@Database(
    entities = [MessageEntity::class, PeerEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun peerDao(): PeerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stealth_encrypted_mesh.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun purgeDatabase(context: Context) {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
                try {
                    context.deleteDatabase("stealth_encrypted_mesh.db")
                } catch (_: Exception) {}
            }
        }
    }
}
