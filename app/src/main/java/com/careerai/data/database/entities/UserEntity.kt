package com.careerai.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "users")
@Serializable
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val displayName: String,
    val profileImageUrl: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val preferences: String, // JSON string for user preferences
    val timezone: String = "UTC",
    val isOnboardingCompleted: Boolean = false
)