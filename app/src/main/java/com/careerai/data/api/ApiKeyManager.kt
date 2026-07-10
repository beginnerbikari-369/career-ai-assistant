package com.careerai.data.api

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.careerai.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val encryptedPrefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        EncryptedSharedPreferences.create(
            context,
            "ai_api_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    fun initializeApiKeys() {
        // Initialize OpenAI API key from BuildConfig if not already set
        if (getOpenAIApiKey().isNullOrBlank() && BuildConfig.OPENAI_API_KEY.isNotBlank()) {
            setOpenAIApiKey(BuildConfig.OPENAI_API_KEY)
        }
        
        // Initialize Google Calendar Client ID if available
        if (getGoogleCalendarClientId().isNullOrBlank() && BuildConfig.GOOGLE_CALENDAR_CLIENT_ID.isNotBlank()) {
            setGoogleCalendarClientId(BuildConfig.GOOGLE_CALENDAR_CLIENT_ID)
        }
    }
    
    fun setOpenAIApiKey(apiKey: String) {
        encryptedPrefs.edit()
            .putString("openai_api_key", apiKey)
            .apply()
    }
    
    fun getOpenAIApiKey(): String? {
        return encryptedPrefs.getString("openai_api_key", null)
    }
    
    fun setGoogleCalendarClientId(clientId: String) {
        encryptedPrefs.edit()
            .putString("google_calendar_client_id", clientId)
            .apply()
    }
    
    fun getGoogleCalendarClientId(): String? {
        return encryptedPrefs.getString("google_calendar_client_id", null)
    }
    
    fun isOpenAIConfigured(): Boolean {
        return !getOpenAIApiKey().isNullOrBlank()
    }
    
    fun isGoogleCalendarConfigured(): Boolean {
        return !getGoogleCalendarClientId().isNullOrBlank()
    }
    
    fun clearAllKeys() {
        encryptedPrefs.edit()
            .clear()
            .apply()
    }
}