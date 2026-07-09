package com.careerai.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "skills")
@Serializable
data class SkillEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val category: String, // technical, soft, language, certification
    val currentLevel: Int, // 1-10 proficiency level
    val targetLevel: Int, // Desired proficiency level
    val description: String?,
    val isCareerRelevant: Boolean = true,
    val priority: String = "medium", // high, medium, low
    val learningResources: String? = null, // JSON array of resources
    val assessmentDate: Long? = null, // Last assessment date
    val certifications: String? = null, // JSON array of certifications
    val projects: String? = null, // JSON array of related projects
    val endorsements: Int = 0, // Number of endorsements
    val createdAt: Long,
    val updatedAt: Long,
    val tags: String? = null, // JSON array of tags
    val isVerified: Boolean = false, // If skill is externally verified
    val linkedInSkillId: String? = null, // LinkedIn skill mapping
    val industryDemand: String? = null // high, medium, low market demand
)