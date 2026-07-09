package com.careerai.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "goals")
@Serializable
data class GoalEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val description: String?,
    val category: String, // career, personal, skill, health, financial
    val priority: String, // high, medium, low
    val type: String, // short_term, long_term, daily, weekly, monthly, yearly
    val targetDate: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long? = null,
    val progress: Int = 0, // 0-100 percentage
    val isCompleted: Boolean = false,
    val isArchived: Boolean = false,
    val parentGoalId: String? = null, // For sub-goals
    val milestones: String? = null, // JSON array of milestones
    val tags: String? = null, // JSON array of tags
    val reminderEnabled: Boolean = true,
    val reminderFrequency: String? = null // daily, weekly, monthly
)