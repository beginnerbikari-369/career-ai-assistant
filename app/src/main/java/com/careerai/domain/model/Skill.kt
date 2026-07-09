package com.careerai.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Skill(
    val id: String,
    val name: String,
    val category: String,
    val currentLevel: Int, // 1-10 scale
    val targetLevel: Int, // 1-10 scale  
    val description: String = "",
    val isCore: Boolean = false, // Core skill for career
    val lastAssessed: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {
    
    val progressPercentage: Int
        get() = if (targetLevel > 0) {
            ((currentLevel.toFloat() / targetLevel) * 100).toInt()
        } else 0
    
    val needsImprovement: Boolean
        get() = currentLevel < targetLevel
    
    val isAdvanced: Boolean
        get() = currentLevel >= 8
}

enum class SkillCategory(val displayName: String) {
    TECHNICAL("Technical"),
    SOFT_SKILLS("Soft Skills"),
    LEADERSHIP("Leadership"),
    COMMUNICATION("Communication"),
    ANALYTICAL("Analytical"),
    CREATIVE("Creative"),
    BUSINESS("Business"),
    OTHER("Other")
}