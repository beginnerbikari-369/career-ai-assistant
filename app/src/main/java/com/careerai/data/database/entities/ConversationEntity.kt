package com.careerai.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "conversations")
@Serializable
data class ConversationEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val context: String, // general, career, goals, habits, planning
    val createdAt: Long,
    val updatedAt: Long,
    val lastMessageAt: Long,
    val messageCount: Int = 0,
    val isArchived: Boolean = false
)