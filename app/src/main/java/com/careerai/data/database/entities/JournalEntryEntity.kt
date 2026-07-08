package com.careerai.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "journal_entries")
@Serializable
data class JournalEntryEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val date: String, // YYYY-MM-DD format
    val title: String?,
    val content: String,
    val mood: String?, // excellent, good, neutral, poor, terrible
    val energy: Int? = null, // 1-10 scale
    val productivity: Int? = null, // 1-10 scale
    val stress: Int? = null, // 1-10 scale
    val gratitude: String? = null, // What user is grateful for
    val highlights: String? = null, // Key highlights of the day
    val challenges: String? = null, // Challenges faced
    val learnings: String? = null, // What was learned
    val tomorrowGoals: String? = null, // Goals for tomorrow
    val tags: String? = null, // JSON array of tags
    val attachments: String? = null, // JSON array of attachment paths
    val createdAt: Long,
    val updatedAt: Long,
    val isPrivate: Boolean = true,
    val weatherData: String? = null, // JSON weather information
    val location: String? = null
)