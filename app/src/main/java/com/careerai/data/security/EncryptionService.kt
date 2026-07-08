package com.careerai.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ENCRYPTION_KEY_ALIAS = "CareerAIEncryptionKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_SEPARATOR = ":"
    }
    
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }
    
    private val encryptedPrefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    init {
        generateOrGetSecretKey()
    }
    
    private fun generateOrGetSecretKey(): SecretKey {
        return if (keyStore.containsAlias(ENCRYPTION_KEY_ALIAS)) {
            keyStore.getKey(ENCRYPTION_KEY_ALIAS, null) as SecretKey
        } else {
            generateSecretKey()
        }
    }
    
    private fun generateSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            ENCRYPTION_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }
    
    fun encryptSensitiveData(plaintext: String): String {
        return try {
            val secretKey = generateOrGetSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val iv = cipher.iv
            val cipherText = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            
            val ivString = Base64.encodeToString(iv, Base64.DEFAULT)
            val cipherString = Base64.encodeToString(cipherText, Base64.DEFAULT)
            
            "$ivString$IV_SEPARATOR$cipherString"
        } catch (e: Exception) {
            throw SecurityException("Encryption failed", e)
        }
    }
    
    fun decryptSensitiveData(encryptedData: String): String {
        return try {
            val parts = encryptedData.split(IV_SEPARATOR)
            if (parts.size != 2) {
                throw SecurityException("Invalid encrypted data format")
            }
            
            val iv = Base64.decode(parts[0], Base64.DEFAULT)
            val cipherText = Base64.decode(parts[1], Base64.DEFAULT)
            
            val secretKey = generateOrGetSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
            
            val plaintext = cipher.doFinal(cipherText)
            String(plaintext, Charsets.UTF_8)
        } catch (e: Exception) {
            throw SecurityException("Decryption failed", e)
        }
    }
    
    fun storeSecureString(key: String, value: String) {
        encryptedPrefs.edit()
            .putString(key, value)
            .apply()
    }
    
    fun getSecureString(key: String, defaultValue: String? = null): String? {
        return encryptedPrefs.getString(key, defaultValue)
    }
    
    fun removeSecureString(key: String) {
        encryptedPrefs.edit()
            .remove(key)
            .apply()
    }
    
    fun clearAllSecureData() {
        encryptedPrefs.edit()
            .clear()
            .apply()
    }
    
    fun encryptJournalEntry(content: String): String {
        // Special handling for journal entries with additional security
        return encryptSensitiveData(content)
    }
    
    fun decryptJournalEntry(encryptedContent: String): String {
        return decryptSensitiveData(encryptedContent)
    }
    
    fun hashPassword(password: String, salt: String): String {
        // For additional local password hashing if needed
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hash = digest.digest((password + salt).toByteArray(Charsets.UTF_8))
            Base64.encodeToString(hash, Base64.DEFAULT)
        } catch (e: Exception) {
            throw SecurityException("Password hashing failed", e)
        }
    }
    
    fun generateSalt(): String {
        val random = java.security.SecureRandom()
        val salt = ByteArray(32)
        random.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.DEFAULT)
    }
    
    fun isKeyStoreAvailable(): Boolean {
        return try {
            keyStore.containsAlias(ENCRYPTION_KEY_ALIAS) || generateOrGetSecretKey() != null
        } catch (e: Exception) {
            false
        }
    }
}