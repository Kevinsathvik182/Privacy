package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "peers")
data class PeerEntity(
    @PrimaryKey val peerId: String,
    val name: String,
    val deviceAddress: String,
    val rssi: Int,
    val estimatedDistanceMeters: Int,
    val isConnected: Boolean,
    val isCodedPhySupported: Boolean,
    val publicKeyFingerprint: String,
    val lastSeenTimestamp: Long
)
