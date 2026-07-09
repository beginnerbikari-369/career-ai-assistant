package com.careerai.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "habits")
@Serializable
data class HabitEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val description: String?,
    val category: String, // health, productivity, learning, wellness, career
    val color: String, // Hex color for UI
    val icon: String?, // Icon identifier
    val frequency: String, // daily, weekly, monthly
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
    val difficulty: String = "medium", // easy, medium, hard
    val tags: String? = null // JSON array of tags
)