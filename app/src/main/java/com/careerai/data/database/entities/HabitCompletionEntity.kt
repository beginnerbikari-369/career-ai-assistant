package com.careerai.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "habit_completions",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habitId"]),
        Index(value = ["date"]),
        Index(value = ["habitId", "date"], unique = true)
    ]
)
@Serializable
data class HabitCompletionEntity(
    @PrimaryKey
    val id: String,
    val habitId: String,
    val date: String, // YYYY-MM-DD format
    val completedAt: Long,
    val completionCount: Int = 1, // How many times completed that day
    val duration: Int? = null, // Actual duration in minutes
    val notes: String? = null,
    val mood: String? = null, // great, good, okay, poor
    val quality: Int? = null // 1-5 rating
)