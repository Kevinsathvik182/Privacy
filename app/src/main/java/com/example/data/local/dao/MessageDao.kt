package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE peerId = :peerId ORDER BY timestamp ASC")
    fun getMessagesForPeer(peerId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM messages")
    suspend fun clearAllMessages()

    @Query("DELETE FROM messages WHERE peerId = :peerId")
    suspend fun deleteMessagesForPeer(peerId: String)
}
