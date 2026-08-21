package com.example.data.model

enum class DeliveryStatus {
    TRANSMITTING,
    SENT_CODED_PHY,
    DELIVERED,
    VERIFIED_E2EE
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val peerId: String,
    val senderName: String,
    val cipherStream: String, // e.g. "4.0 4.5"
    val rawText: String,      // Plaintext when decrypted, e.g. "HI"
    val encryptedBlob: String,// AES-256-GCM Base64 envelope
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean = true,
    val deliveryStatus: DeliveryStatus = DeliveryStatus.SENT_CODED_PHY,
    val distanceEstimateMeters: Int = 15
)

data class MeshPeer(
    val peerId: String,
    val name: String,
    val deviceAddress: String,
    val rssi: Int = -68,
    val estimatedDistanceMeters: Int = 45,
    val isConnected: Boolean = false,
    val isCodedPhySupported: Boolean = true,
    val publicKeyFingerprint: String = "9B:4A:2C:FE",
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val isSimulated: Boolean = false
)

data class DecoyTrack(
    val id: String,
    val title: String,
    val artist: String,
    val durationSeconds: Int,
    val category: String = "Lo-Fi Beats"
)

data class StudyDocument(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val pageCount: Int,
    val contentPreview: String
)

enum class VaultNavTab {
    RADAR,
    CHAT,
    CIPHER_LAB,
    SECURITY_AUDIT
}

enum class DecoyTab {
    MUSIC,
    STUDY_PDFS,
    EQUALIZER
}
