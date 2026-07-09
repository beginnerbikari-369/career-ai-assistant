package com.careerai.data.repository

import com.careerai.data.database.dao.SkillDao
import com.careerai.data.database.entities.SkillEntity
import com.careerai.domain.model.Skill
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkillRepository @Inject constructor(
    private val skillDao: SkillDao
) {
    
    fun getSkillsFlow(userId: String): Flow<List<Skill>> {
        return skillDao.getSkillsFlow(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    fun getCareerRelevantSkillsFlow(userId: String): Flow<List<Skill>> {
        return skillDao.getCareerRelevantSkillsFlow(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    fun getSkillsNeedingImprovementFlow(userId: String): Flow<List<Skill>> {
        return skillDao.getSkillsNeedingImprovementFlow(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    suspend fun addSkill(
        userId: String,
        name: String,
        category: String,
        currentLevel: Int,
        targetLevel: Int,
        description: String? = null
    ): Result<String> {
        return try {
            val skillId = UUID.randomUUID().toString()
            val timestamp = System.currentTimeMillis()
            
            val skill = SkillEntity(
                id = skillId,
                userId = userId,
                name = name,
                category = category,
                currentLevel = currentLevel,
                targetLevel = targetLevel,
                description = description,
                createdAt = timestamp,
                updatedAt = timestamp,
                assessmentDate = timestamp
            )
            
            skillDao.insertSkill(skill)
            Result.success(skillId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateSkillLevel(skillId: String, newLevel: Int): Result<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()
            skillDao.updateSkillLevel(skillId, newLevel, timestamp, timestamp)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Domain mapping
private fun SkillEntity.toDomain(): Skill {
    return Skill(
        id = id,
        userId = userId,
        name = name,
        category = category,
        currentLevel = currentLevel,
        targetLevel = targetLevel,
        description = description,
        isCareerRelevant = isCareerRelevant,
        priority = priority,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

data class Skill(
    val id: String,
    val userId: String,
    val name: String,
    val category: String,
    val currentLevel: Int,
    val targetLevel: Int,
    val description: String?,
    val isCareerRelevant: Boolean,
    val priority: String,
    val createdAt: Long,
    val updatedAt: Long
)