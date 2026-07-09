package com.careerai.utils

object Constants {
    
    // API Configuration
    const val OPENAI_BASE_URL = "https://api.openai.com/"
    const val DEFAULT_AI_MODEL = "gpt-4"
    const val MAX_TOKENS_DEFAULT = 2000
    const val TEMPERATURE_DEFAULT = 0.7f
    
    // Database
    const val DATABASE_VERSION = 1
    const val DATABASE_NAME = "career_ai_database"
    
    // Shared Preferences
    const val PREFS_NAME = "career_ai_prefs"
    const val PREFS_USER_ONBOARDED = "user_onboarded"
    const val PREFS_THEME_MODE = "theme_mode"
    const val PREFS_NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val PREFS_SYNC_ENABLED = "sync_enabled"
    
    // Work Manager
    const val SYNC_WORK_NAME = "sync_work"
    const val NOTIFICATION_WORK_TAG = "notification_work"
    const val CALENDAR_SYNC_WORK_NAME = "calendar_sync_work"
    
    // Notification Channels
    const val CHANNEL_ID_REMINDERS = "reminders"
    const val CHANNEL_ID_HABITS = "habits"
    const val CHANNEL_ID_AI_SUGGESTIONS = "ai_suggestions"
    const val CHANNEL_ID_GOALS = "goals"
    const val CHANNEL_ID_CALENDAR = "calendar"
    
    // Date Formats
    const val DATE_FORMAT_DISPLAY = "MMM dd, yyyy"
    const val DATE_FORMAT_API = "yyyy-MM-dd"
    const val TIME_FORMAT_DISPLAY = "HH:mm"
    const val DATETIME_FORMAT_API = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    
    // Timeouts
    const val NETWORK_TIMEOUT_SECONDS = 30L
    const val SYNC_TIMEOUT_MINUTES = 5L
    
    // Limits
    const val MAX_CONVERSATION_MESSAGES = 1000
    const val MAX_HABIT_STREAK_DISPLAY = 999
    const val MAX_GOAL_TITLE_LENGTH = 100
    const val MAX_HABIT_NAME_LENGTH = 50
    const val MAX_MESSAGE_LENGTH = 4000
    
    // Feature Flags
    const val FEATURE_VOICE_INPUT = true
    const val FEATURE_CALENDAR_INTEGRATION = true
    const val FEATURE_AI_SUGGESTIONS = true
    const val FEATURE_CLOUD_SYNC = true
    const val FEATURE_ANALYTICS = true
    
    // Error Messages
    const val ERROR_NETWORK = "Network connection error. Please check your internet connection."
    const val ERROR_AI_SERVICE = "AI service is temporarily unavailable. Please try again later."
    const val ERROR_SYNC_FAILED = "Failed to sync data. Changes will be synced when connection is restored."
    const val ERROR_AUTHENTICATION = "Authentication failed. Please sign in again."
    const val ERROR_PERMISSION_DENIED = "Permission denied. Please grant required permissions in settings."
    
    // Success Messages
    const val SUCCESS_GOAL_CREATED = "Goal created successfully!"
    const val SUCCESS_HABIT_COMPLETED = "Great job! Habit completed."
    const val SUCCESS_DATA_EXPORTED = "Data exported successfully to Downloads folder."
    const val SUCCESS_SYNC_COMPLETED = "Data synced successfully."
    
    // Animation Durations (milliseconds)
    const val ANIM_DURATION_SHORT = 150
    const val ANIM_DURATION_MEDIUM = 300
    const val ANIM_DURATION_LONG = 500
    
    // UI Constants
    const val BOTTOM_SHEET_PEEK_HEIGHT_DP = 64
    const val FAB_SIZE_DP = 56
    const val CARD_ELEVATION_DP = 4
    const val PROGRESS_BAR_HEIGHT_DP = 8
    
    // Analytics Events
    const val EVENT_GOAL_CREATED = "goal_created"
    const val EVENT_HABIT_COMPLETED = "habit_completed"
    const val EVENT_AI_MESSAGE_SENT = "ai_message_sent"
    const val EVENT_CALENDAR_EVENT_CREATED = "calendar_event_created"
    const val EVENT_DATA_EXPORTED = "data_exported"
    
    // Privacy and Security
    const val MIN_PASSWORD_LENGTH = 8
    const val SESSION_TIMEOUT_MINUTES = 30
    const val MAX_LOGIN_ATTEMPTS = 5
    const val LOCKOUT_DURATION_MINUTES = 15
}