package com.careerai.domain.model

data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val profileImageUrl: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val preferences: UserPreferences,
    val timezone: String = "UTC",
    val isOnboardingCompleted: Boolean = false
)

data class UserPreferences(
    val theme: String = "system", // light, dark, system
    val language: String = "en",
    val notificationsEnabled: Boolean = true,
    val calendarReminderMinutes: List<Int> = listOf(15, 30),
    val habitRemindersEnabled: Boolean = true,
    val aiSuggestionsEnabled: Boolean = true,
    val voiceInputEnabled: Boolean = true,
    val dataSync: Boolean = true,
    val weekStartDay: Int = 1, // 1 = Monday, 7 = Sunday
    val timeFormat: String = "24h", // 12h, 24h
    val defaultAIModel: String = "gpt-4"
)