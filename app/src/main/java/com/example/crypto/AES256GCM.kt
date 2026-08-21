package com.example.crypto

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object AES256GCM {
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val AES_KEY_SIZE_BITS = 256
    private const val GCM_IV_LENGTH_BYTES = 12
    private const val GCM_TAG_LENGTH_BITS = 128

    private val secureRandom = SecureRandom()

    /**
     * Generates a fresh 256-bit AES secret key.
     */
    fun generateKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(AES_KEY_SIZE_BITS, secureRandom)
        return keyGen.generateKey()
    }

    /**
     * Encrypts plaintext string using AES-256-GCM.
     * Returns Base64 string containing [12-byte IV + ciphertext with 128-bit auth tag].
     */
    fun encrypt(plainText: String, secretKey: SecretKey): String {
        val iv = ByteArray(GCM_IV_LENGTH_BYTES).also { secureRandom.nextBytes(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val combined = ByteArray(iv.size + cipherBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Decrypts Base64 string containing [12-byte IV + ciphertext with 128-bit auth tag].
     */
    fun decrypt(base64Payload: String, secretKey: SecretKey): String {
        val combined = Base64.decode(base64Payload, Base64.NO_WRAP)
        if (combined.size < GCM_IV_LENGTH_BYTES) {
            throw IllegalArgumentException("Payload too short for GCM IV")
        }

        val iv = ByteArray(GCM_IV_LENGTH_BYTES)
        System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH_BYTES)

        val cipherBytes = ByteArray(combined.size - GCM_IV_LENGTH_BYTES)
        System.arraycopy(combined, GCM_IV_LENGTH_BYTES, cipherBytes, 0, cipherBytes.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        val plainBytes = cipher.doFinal(cipherBytes)
        return String(plainBytes, Charsets.UTF_8)
    }

    /**
     * Converts a raw key byte array to a SecretKeySpec.
     */
    fun keyFromBytes(keyBytes: ByteArray): SecretKey {
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Derives a deterministic 256-bit AES key from a passphrase/shared secret using SHA-256.
     */
    fun deriveKeyFromSecret(secret: String): SecretKey {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(secret.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }
}
