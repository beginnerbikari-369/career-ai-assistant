package com.careerai.domain.model

data class Goal(
    val id: String,
    val userId: String,
    val title: String,
    val description: String?,
    val category: GoalCategory,
    val priority: Priority,
    val type: GoalType,
    val targetDate: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long? = null,
    val progress: Int = 0, // 0-100 percentage
    val isCompleted: Boolean = false,
    val isArchived: Boolean = false,
    val parentGoalId: String? = null,
    val milestones: List<Milestone> = emptyList(),
    val tags: List<String> = emptyList(),
    val reminderEnabled: Boolean = true,
    val reminderFrequency: ReminderFrequency? = null,
    val subGoals: List<Goal> = emptyList()
)

enum class GoalCategory {
    CAREER,
    PERSONAL,
    SKILL,
    HEALTH,
    FINANCIAL,
    EDUCATION,
    RELATIONSHIP,
    TRAVEL,
    OTHER
}

enum class Priority {
    HIGH,
    MEDIUM,
    LOW
}

enum class GoalType {
    SHORT_TERM,
    LONG_TERM,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

enum class ReminderFrequency {
    DAILY,
    WEEKLY,
    MONTHLY
}

data class Milestone(
    val id: String,
    val title: String,
    val description: String?,
    val targetDate: Long?,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)