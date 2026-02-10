package com.example.niramaya.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoManager {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val ALIAS = "niramaya_medical_key"

    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    private fun getSecretKey(): SecretKey {
        // If key exists, return it
        keyStore.getKey(ALIAS, null)?.let { return it as SecretKey }

        // Else, generate a new one in the Hardware Security Module
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )

        return keyGenerator.generateKey()
    }

    fun encrypt(data: String): String {
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

            // We must store the IV (Initialization Vector) to decrypt later
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

            // Format: IV_length + IV + EncryptedData (all Base64 encoded)
            // We combine IV and Data into one string for easy storage in Firestore
            val combined = ByteArray(1 + iv.size + encryptedBytes.size)
            combined[0] = iv.size.toByte()
            System.arraycopy(iv, 0, combined, 1, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, 1 + iv.size, encryptedBytes.size)

            return Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            return data // Fail-safe: return original if crypto fails
        }
    }

    fun decrypt(encryptedData: String): String {
        try {
            // If it's not Base64 (e.g. old plain data), return as is
            val decoded = try {
                Base64.decode(encryptedData, Base64.DEFAULT)
            } catch (e: IllegalArgumentException) {
                return encryptedData
            }

            // Parse the IV length, IV, and Data
            val ivSize = decoded[0].toInt()
            val iv = ByteArray(ivSize)
            val encryptedBytes = ByteArray(decoded.size - 1 - ivSize)

            System.arraycopy(decoded, 1, iv, 0, ivSize)
            System.arraycopy(decoded, 1 + ivSize, encryptedBytes, 0, encryptedBytes.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv) // 128 bit auth tag length
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

            return String(cipher.doFinal(encryptedBytes), Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            return "Decryption Failed" // Or return original text
        }
    }
}