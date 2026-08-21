package com.example.crypto

import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

data class EphemeralSessionKeys(
    val keyPair: KeyPair,
    val publicKeyBase64: String,
    val keyFingerprint: String
)

object KeyExchange {
    private const val ALGORITHM = "EC"
    private const val CURVE_NAME = "secp256r1"

    /**
     * Generates an ephemeral Elliptic Curve key pair for offline session key agreement.
     */
    fun generateEphemeralKeys(): EphemeralSessionKeys {
        val kpg = KeyPairGenerator.getInstance(ALGORITHM)
        val ecSpec = ECGenParameterSpec(CURVE_NAME)
        kpg.initialize(ecSpec)
        val pair = kpg.generateKeyPair()

        val pubBytes = pair.public.encoded
        val pubBase64 = Base64.encodeToString(pubBytes, Base64.NO_WRAP)
        val fingerprint = computeFingerprint(pubBytes)

        return EphemeralSessionKeys(
            keyPair = pair,
            publicKeyBase64 = pubBase64,
            keyFingerprint = fingerprint
        )
    }

    /**
     * Computes shared AES-256 SecretKey from our private key and peer's public key (Diffie-Hellman / ECDH).
     */
    fun computeSharedSecret(myPrivateKey: PrivateKey, peerPublicKeyBase64: String): SecretKey {
        val pubBytes = Base64.decode(peerPublicKeyBase64, Base64.NO_WRAP)
        val keyFactory = KeyFactory.getInstance(ALGORITHM)
        val peerPublicKey: PublicKey = keyFactory.generatePublic(X509EncodedKeySpec(pubBytes))

        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(myPrivateKey)
        keyAgreement.doPhase(peerPublicKey, true)

        val rawSharedSecret = keyAgreement.generateSecret()
        // Hash shared secret with SHA-256 to produce pristine 256-bit AES key
        val digest = MessageDigest.getInstance("SHA-256")
        val derivedKeyBytes = digest.digest(rawSharedSecret)

        return SecretKeySpec(derivedKeyBytes, "AES")
    }

    /**
     * Computes a human-readable 8-char SHA-256 fingerprint for public key identity verification.
     */
    fun computeFingerprint(keyBytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(keyBytes)
        val hex = hash.take(4).joinToString(":") { "%02X".format(it) }
        return hex
    }
}
