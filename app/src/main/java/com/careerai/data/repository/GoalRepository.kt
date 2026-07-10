package com.careerai.data.repository

import com.careerai.data.database.dao.GoalDao
import com.careerai.data.database.entities.GoalEntity
import com.careerai.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao
) {
    
    fun getGoalsFlow(userId: String): Flow<List<Goal>> {
        return goalDao.getActiveGoalsFlow(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    fun getIncompleteGoalsFlow(userId: String): Flow<List<Goal>> {
        return goalDao.getIncompleteGoalsFlow(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    fun getGoalsByCategoryFlow(userId: String, category: GoalCategory): Flow<List<Goal>> {
        return goalDao.getGoalsByCategoryFlow(userId, category.name.lowercase())
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    fun getGoalByIdFlow(goalId: String): Flow<Goal?> {
        return goalDao.getGoalByIdFlow(goalId)
            .map { entity -> entity?.toDomain() }
    }
    
    fun getSubGoalsFlow(userId: String, parentGoalId: String): Flow<List<Goal>> {
        return goalDao.getSubGoalsFlow(userId, parentGoalId)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    fun getCompletedGoalsFlow(userId: String): Flow<List<Goal>> {
        return goalDao.getCompletedGoalsFlow(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    fun getCompletedGoalsFlow(userId: String, limit: Int): Flow<List<Goal>> {
        return goalDao.getCompletedGoalsFlow(userId)
            .map { entities -> entities.take(limit).map { it.toDomain() } }
    }
    
    suspend fun createGoal(
        userId: String,
        title: String,
        description: String?,
        category: GoalCategory,
        priority: Priority,
        type: GoalType,
        targetDate: Long? = null,
        parentGoalId: String? = null,
        milestones: List<Milestone> = emptyList(),
        tags: List<String> = emptyList()
    ): Result<String> {
        return try {
            val goalId = UUID.randomUUID().toString()
            val timestamp = System.currentTimeMillis()
            
            val goalEntity = GoalEntity(
                id = goalId,
                userId = userId,
                title = title,
                description = description,
                category = category.name.lowercase(),
                priority = priority.name.lowercase(),
                type = type.name.lowercase(),
                targetDate = targetDate,
                createdAt = timestamp,
                updatedAt = timestamp,
                parentGoalId = parentGoalId,
                milestones = if (milestones.isNotEmpty()) Json.encodeToString(milestones) else null,
                tags = if (tags.isNotEmpty()) Json.encodeToString(tags) else null
            )
            
            goalDao.insertGoal(goalEntity)
            Result.success(goalId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateGoal(goal: Goal): Result<Unit> {
        return try {
            val entity = goal.toEntity()
            goalDao.updateGoal(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateGoalProgress(goalId: String, progress: Int): Result<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()
            goalDao.updateProgress(goalId, progress, timestamp)
            
            // Auto-complete if progress reaches 100%
            if (progress >= 100) {
                goalDao.updateCompletionStatus(goalId, true, timestamp, 100, timestamp)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun completeGoal(goalId: String): Result<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()
            goalDao.updateCompletionStatus(goalId, true, timestamp, 100, timestamp)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun archiveGoal(goalId: String): Result<Unit> {
        return try {
            goalDao.updateArchivedStatus(goalId, true, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteGoal(goalId: String): Result<Unit> {
        return try {
            val goal = goalDao.getGoalById(goalId)
            goal?.let { goalDao.deleteGoal(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getOverdueGoals(userId: String): Result<List<Goal>> {
        return try {
            val currentTime = System.currentTimeMillis()
            val goals = goalDao.getOverdueGoals(userId, currentTime)
            Result.success(goals.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getGoalStatistics(userId: String): Result<GoalStatistics> {
        return try {
            val totalGoals = goalDao.getActiveGoalsCount(userId)
            val completedGoals = goalDao.getCompletedGoalsCount(userId)
            val averageProgress = goalDao.getAverageProgress(userId) ?: 0.0
            
            val statistics = GoalStatistics(
                totalGoals = totalGoals,
                completedGoals = completedGoals,
                activeGoals = totalGoals,
                averageProgress = averageProgress.toInt(),
                completionRate = if (totalGoals > 0) {
                    (completedGoals.toFloat() / (totalGoals + completedGoals) * 100).toInt()
                } else 0
            )
            
            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Extension functions for domain mapping
private fun GoalEntity.toDomain(): Goal {
    val milestonesJson = milestones
    val tagsJson = tags
    
    return Goal(
        id = id,
        userId = userId,
        title = title,
        description = description,
        category = GoalCategory.valueOf(category.uppercase()),
        priority = Priority.valueOf(priority.uppercase()),
        type = GoalType.valueOf(type.uppercase()),
        targetDate = targetDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        completedAt = completedAt,
        progress = progress,
        isCompleted = isCompleted,
        isArchived = isArchived,
        parentGoalId = parentGoalId,
        milestones = milestonesJson?.let {
            try {
                Json.decodeFromString<List<Milestone>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList(),
        tags = tagsJson?.let {
            try {
                Json.decodeFromString<List<String>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList(),
        reminderEnabled = reminderEnabled,
        reminderFrequency = reminderFrequency?.let { ReminderFrequency.valueOf(it.uppercase()) }
    )
}

private fun Goal.toEntity(): GoalEntity {
    return GoalEntity(
        id = id,
        userId = userId,
        title = title,
        description = description,
        category = category.name.lowercase(),
        priority = priority.name.lowercase(),
        type = type.name.lowercase(),
        targetDate = targetDate,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis(),
        completedAt = completedAt,
        progress = progress,
        isCompleted = isCompleted,
        isArchived = isArchived,
        parentGoalId = parentGoalId,
        milestones = if (milestones.isNotEmpty()) Json.encodeToString(milestones) else null,
        tags = if (tags.isNotEmpty()) Json.encodeToString(tags) else null,
        reminderEnabled = reminderEnabled,
        reminderFrequency = reminderFrequency?.name?.lowercase()
    )
}

data class GoalStatistics(
    val totalGoals: Int,
    val completedGoals: Int,
    val activeGoals: Int,
    val averageProgress: Int,
    val completionRate: Int
)