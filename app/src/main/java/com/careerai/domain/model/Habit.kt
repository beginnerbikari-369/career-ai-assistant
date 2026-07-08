package com.careerai.domain.model

data class Habit(
    val id: String,
    val userId: String,
    val name: String,
    val description: String?,
    val category: HabitCategory,
    val color: String, // Hex color for UI
    val icon: String?, // Icon identifier
    val frequency: HabitFrequency,
    val targetCount: Int = 1, // How many times per frequency period
    val duration: Int? = null, // Duration in minutes if applicable
    val reminderTime: String?, // HH:mm format
    val isReminderEnabled: Boolean = true,
    val streakCount: Int = 0,
    val longestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean = true,
    val isArchived: Boolean = false,
    val difficulty: HabitDifficulty = HabitDifficulty.MEDIUM,
    val tags: List<String> = emptyList(),
    val completions: List<HabitCompletion> = emptyList()
)

enum class HabitCategory {
    HEALTH,
    PRODUCTIVITY,
    LEARNING,
    WELLNESS,
    CAREER,
    SOCIAL,
    FINANCIAL,
    CREATIVITY,
    OTHER
}

enum class HabitFrequency {
    DAILY,
    WEEKLY,
    MONTHLY
}

enum class HabitDifficulty {
    EASY,
    MEDIUM,
    HARD
}

data class HabitCompletion(
    val id: String,
    val habitId: String,
    val date: String, // YYYY-MM-DD format
    val completedAt: Long,
    val completionCount: Int = 1, // How many times completed that day
    val duration: Int? = null, // Actual duration in minutes
    val notes: String? = null,
    val mood: CompletionMood? = null,
    val quality: Int? = null // 1-5 rating
)

enum class CompletionMood {
    GREAT,
    GOOD,
    OKAY,
    POOR
}