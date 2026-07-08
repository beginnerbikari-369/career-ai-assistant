package com.careerai.data.database.dao

import androidx.room.*
import com.careerai.data.database.entities.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    
    @Query("SELECT * FROM goals WHERE userId = :userId AND isArchived = 0 ORDER BY createdAt DESC")
    fun getActiveGoalsFlow(userId: String): Flow<List<GoalEntity>>
    
    @Query("SELECT * FROM goals WHERE userId = :userId AND isCompleted = 0 AND isArchived = 0 ORDER BY priority DESC, targetDate ASC")
    fun getIncompleteGoalsFlow(userId: String): Flow<List<GoalEntity>>
    
    @Query("SELECT * FROM goals WHERE userId = :userId AND category = :category AND isArchived = 0 ORDER BY createdAt DESC")
    fun getGoalsByCategoryFlow(userId: String, category: String): Flow<List<GoalEntity>>
    
    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: String): GoalEntity?
    
    @Query("SELECT * FROM goals WHERE id = :goalId")
    fun getGoalByIdFlow(goalId: String): Flow<GoalEntity?>
    
    @Query("SELECT * FROM goals WHERE userId = :userId AND parentGoalId = :parentGoalId AND isArchived = 0")
    fun getSubGoalsFlow(userId: String, parentGoalId: String): Flow<List<GoalEntity>>
    
    @Query("SELECT * FROM goals WHERE userId = :userId AND isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedGoalsFlow(userId: String): Flow<List<GoalEntity>>
    
    @Query("SELECT * FROM goals WHERE userId = :userId AND targetDate <= :date AND isCompleted = 0 AND isArchived = 0")
    suspend fun getOverdueGoals(userId: String, date: Long): List<GoalEntity>
    
    @Query("SELECT * FROM goals WHERE userId = :userId AND targetDate BETWEEN :startDate AND :endDate AND isArchived = 0")
    suspend fun getGoalsByDateRange(userId: String, startDate: Long, endDate: Long): List<GoalEntity>
    
    @Query("SELECT COUNT(*) FROM goals WHERE userId = :userId AND isCompleted = 1")
    suspend fun getCompletedGoalsCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM goals WHERE userId = :userId AND isCompleted = 0 AND isArchived = 0")
    suspend fun getActiveGoalsCount(userId: String): Int
    
    @Query("SELECT AVG(progress) FROM goals WHERE userId = :userId AND isCompleted = 0 AND isArchived = 0")
    suspend fun getAverageProgress(userId: String): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<GoalEntity>)
    
    @Update
    suspend fun updateGoal(goal: GoalEntity)
    
    @Delete
    suspend fun deleteGoal(goal: GoalEntity)
    
    @Query("UPDATE goals SET progress = :progress, updatedAt = :timestamp WHERE id = :goalId")
    suspend fun updateProgress(goalId: String, progress: Int, timestamp: Long)
    
    @Query("UPDATE goals SET isCompleted = :isCompleted, completedAt = :completedAt, progress = :progress, updatedAt = :timestamp WHERE id = :goalId")
    suspend fun updateCompletionStatus(goalId: String, isCompleted: Boolean, completedAt: Long?, progress: Int, timestamp: Long)
    
    @Query("UPDATE goals SET isArchived = :isArchived, updatedAt = :timestamp WHERE id = :goalId")
    suspend fun updateArchivedStatus(goalId: String, isArchived: Boolean, timestamp: Long)
    
    @Query("DELETE FROM goals WHERE userId = :userId")
    suspend fun deleteAllUserGoals(userId: String)
}