package com.careerai.data.api

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.careerai.domain.model.ConversationContext
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val retrofit: Retrofit
) {
    
    private val apiService: AIApiService by lazy {
        retrofit.newBuilder()
            .baseUrl("https://api.openai.com/")
            .build()
            .create(AIApiService::class.java)
    }
    
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
    
    fun setApiKey(apiKey: String) {
        encryptedPrefs.edit()
            .putString("openai_api_key", apiKey)
            .apply()
    }
    
    fun getApiKey(): String? {
        return encryptedPrefs.getString("openai_api_key", null)
    }
    
    suspend fun sendMessage(
        messages: List<ChatMessage>,
        context: ConversationContext = ConversationContext.GENERAL,
        model: String = "gpt-4",
        maxTokens: Int? = null
    ): Result<ChatResponse> {
        return try {
            val apiKey = getApiKey()
            if (apiKey.isNullOrBlank()) {
                return Result.failure(Exception("API key not configured"))
            }
            
            val systemMessage = getSystemPromptForContext(context)
            val allMessages = if (systemMessage != null) {
                listOf(systemMessage) + messages
            } else {
                messages
            }
            
            val request = ChatRequest(
                model = model,
                messages = allMessages,
                max_tokens = maxTokens,
                temperature = getTemperatureForContext(context)
            )
            
            val response = apiService.sendChatMessage(
                authorization = "Bearer $apiKey",
                request = request
            )
            
            if (response.isSuccessful) {
                val chatResponse = response.body()
                if (chatResponse != null) {
                    Result.success(chatResponse)
                } else {
                    Result.failure(Exception("Empty response from AI service"))
                }
            } else {
                Result.failure(Exception("AI service error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun getSystemPromptForContext(context: ConversationContext): ChatMessage? {
        val prompt = when (context) {
            ConversationContext.GENERAL -> """
                You are a helpful Career AI Assistant. You help users with career development, 
                daily routines, goal setting, and personal productivity. Be encouraging, 
                practical, and provide actionable advice.
            """.trimIndent()
            
            ConversationContext.CAREER -> """
                You are a Career AI Assistant specializing in career development. Help users with:
                - Job search strategies and interview preparation
                - Career path planning and advancement
                - Skill development recommendations
                - Resume and LinkedIn optimization
                - Professional networking advice
                - Salary negotiation guidance
                
                Be professional, encouraging, and provide specific, actionable career advice.
            """.trimIndent()
            
            ConversationContext.GOALS -> """
                You are a Goal Achievement Coach AI. Help users with:
                - Setting SMART goals (Specific, Measurable, Achievable, Relevant, Time-bound)
                - Breaking down large goals into smaller actionable steps
                - Creating accountability systems
                - Overcoming obstacles and setbacks
                - Tracking progress and celebrating milestones
                
                Be motivational, practical, and focus on actionable strategies.
            """.trimIndent()
            
            ConversationContext.HABITS -> """
                You are a Habit Formation AI Coach. Help users with:
                - Building positive habits and breaking bad ones
                - Creating effective habit stacks
                - Understanding habit loops (cue, routine, reward)
                - Designing environment for success
                - Tracking progress and maintaining streaks
                - Handling habit setbacks and relapses
                
                Be supportive, understanding, and provide science-based habit advice.
            """.trimIndent()
            
            ConversationContext.PLANNING -> """
                You are a Productivity and Planning AI Assistant. Help users with:
                - Daily, weekly, and monthly planning strategies
                - Time management and prioritization
                - Calendar optimization and time blocking
                - Work-life balance
                - Energy management throughout the day
                - Creating effective routines and schedules
                
                Be practical, organized, and focus on systems that improve productivity.
            """.trimIndent()
        }
        
        return ChatMessage(role = "system", content = prompt)
    }
    
    private fun getTemperatureForContext(context: ConversationContext): Float {
        return when (context) {
            ConversationContext.CAREER -> 0.3f // More focused and professional
            ConversationContext.GOALS -> 0.5f // Balanced creativity and focus
            ConversationContext.HABITS -> 0.4f // Slightly more focused
            ConversationContext.PLANNING -> 0.3f // More structured and organized
            ConversationContext.GENERAL -> 0.7f // More creative and conversational
        }
    }
    
    fun isApiKeyConfigured(): Boolean {
        return !getApiKey().isNullOrBlank()
    }
    
    suspend fun testConnection(): Result<Boolean> {
        return try {
            val testMessages = listOf(
                ChatMessage(role = "user", content = "Hello, are you working?")
            )
            
            val result = sendMessage(
                messages = testMessages,
                model = "gpt-3.5-turbo",
                maxTokens = 10
            )
            
            result.fold(
                onSuccess = { Result.success(true) },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}