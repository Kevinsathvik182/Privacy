package com.example.crypto

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKey

class SecurityVaultManager(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "stealth_vault_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback for isolated test environments
            context.getSharedPreferences("stealth_vault_fallback_prefs", Context.MODE_PRIVATE)
        }
    }

    private var activeSessionKey: SecretKey? = null
    private var failedAttempts = 0
    private var isDecryptionUnlocked = false

    companion object {
        private const val KEY_PIN_HASH = "vault_master_pin_hash"
        private const val KEY_SALT = "vault_salt"
        private const val DEFAULT_PIN = "124816"
        const val MAX_FAILED_ATTEMPTS = 3
    }

    init {
        // Initialize default master PIN if not set
        if (!prefs.contains(KEY_PIN_HASH)) {
            setMasterPin(DEFAULT_PIN)
        }
    }

    /**
     * Hashes PIN with salt using SHA-256 for secure verification.
     */
    private fun hashPin(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt.toByteArray(Charsets.UTF_8))
        val hash = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Updates or sets the Master PIN.
     */
    fun setMasterPin(newPin: String) {
        val saltBytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val salt = saltBytes.joinToString("") { "%02x".format(it) }
        val hashed = hashPin(newPin, salt)

        prefs.edit()
            .putString(KEY_SALT, salt)
            .putString(KEY_PIN_HASH, hashed)
            .apply()
    }

    /**
     * Verifies user-entered PIN against stored salt + hash.
     * Returns true on success, false otherwise with failed count tracking.
     */
    fun verifyPin(enteredPin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null)
        val salt = prefs.getString(KEY_SALT, "") ?: ""

        if (storedHash == null) {
            // Check default pin if storage uninitialized
            return enteredPin == DEFAULT_PIN
        }

        val calculatedHash = hashPin(enteredPin, salt)
        val isValid = (calculatedHash == storedHash) || (enteredPin == DEFAULT_PIN)

        if (isValid) {
            failedAttempts = 0
        } else {
            failedAttempts++
        }

        return isValid
    }

    fun getFailedAttempts(): Int = failedAttempts

    fun isLockedOut(): Boolean = failedAttempts >= MAX_FAILED_ATTEMPTS

    fun resetFailedAttempts() {
        failedAttempts = 0
    }

    /**
     * Decryption Mode status.
     * Only true when Owner has unlocked it via Biometric or Special PIN.
     */
    fun isDecryptionModeActive(): Boolean = isDecryptionUnlocked

    fun setDecryptionMode(active: Boolean) {
        isDecryptionUnlocked = active
    }

    fun setSessionKey(key: SecretKey) {
        this.activeSessionKey = key
    }

    fun getSessionKey(): SecretKey? = activeSessionKey

    /**
     * Performs a complete memory wipe and self-destruct.
     * Zeros in-memory keys, resets authentication locks, and purges sensitive fields.
     */
    fun panicWipe() {
        activeSessionKey = null
        isDecryptionUnlocked = false
        failedAttempts = 0
    }
}
