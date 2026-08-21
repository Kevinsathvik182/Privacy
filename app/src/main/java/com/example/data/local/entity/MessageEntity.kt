package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val peerId: String,
    val senderName: String,
    val cipherStream: String,
    val rawText: String,
    val encryptedBlob: String,
    val timestamp: Long,
    val isFromMe: Boolean,
    val deliveryStatus: String,
    val distanceEstimateMeters: Int
)
