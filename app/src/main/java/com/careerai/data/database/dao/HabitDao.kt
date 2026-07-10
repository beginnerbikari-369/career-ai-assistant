package com.careerai.data.database.dao

import androidx.room.*
import com.careerai.data.database.entities.HabitEntity
import com.careerai.data.database.entities.HabitCompletionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    
    @Query("SELECT * FROM habits WHERE userId = :userId AND isActive = 1 AND isArchived = 0 ORDER BY createdAt ASC")
    fun getActiveHabitsFlow(userId: String): Flow<List<HabitEntity>>
    
    @Query("SELECT * FROM habits WHERE userId = :userId AND category = :category AND isActive = 1 AND isArchived = 0")
    fun getHabitsByCategoryFlow(userId: String, category: String): Flow<List<HabitEntity>>
    
    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitById(habitId: String): HabitEntity?
    
    @Query("SELECT * FROM habits WHERE id = :habitId")
    fun getHabitByIdFlow(habitId: String): Flow<HabitEntity?>
    
    @Query("SELECT * FROM habits WHERE userId = :userId AND isReminderEnabled = 1 AND isActive = 1 AND isArchived = 0")
    suspend fun getHabitsWithReminders(userId: String): List<HabitEntity>
    
    @Query("SELECT COUNT(*) FROM habits WHERE userId = :userId AND isActive = 1 AND isArchived = 0")
    suspend fun getActiveHabitsCount(userId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>)
    
    @Update
    suspend fun updateHabit(habit: HabitEntity)
    
    @Delete
    suspend fun deleteHabit(habit: HabitEntity)
    
    @Query("UPDATE habits SET streakCount = :streakCount, longestStreak = :longestStreak, totalCompletions = totalCompletions + 1, updatedAt = :timestamp WHERE id = :habitId")
    suspend fun updateHabitStats(habitId: String, streakCount: Int, longestStreak: Int, timestamp: Long)
    
    @Query("UPDATE habits SET streakCount = :streakCount, longestStreak = :longestStreak, totalCompletions = :totalCompletions, updatedAt = :timestamp WHERE id = :habitId")
    suspend fun updateHabitStats(habitId: String, streakCount: Int, longestStreak: Int, timestamp: Long, totalCompletions: Int)
    
    @Query("UPDATE habits SET isActive = :isActive, updatedAt = :timestamp WHERE id = :habitId")
    suspend fun updateActiveStatus(habitId: String, isActive: Boolean, timestamp: Long)
    
    @Query("UPDATE habits SET isArchived = :isArchived, updatedAt = :timestamp WHERE id = :habitId")
    suspend fun updateArchivedStatus(habitId: String, isArchived: Boolean, timestamp: Long)
    
    @Query("DELETE FROM habits WHERE userId = :userId")
    suspend fun deleteAllUserHabits(userId: String)
    
    // Habit Completions
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC")
    fun getHabitCompletionsFlow(habitId: String): Flow<List<HabitCompletionEntity>>
    
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun getHabitCompletion(habitId: String, date: String): HabitCompletionEntity?
    
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getHabitCompletionsInRange(habitId: String, startDate: String, endDate: String): List<HabitCompletionEntity>
    
    @Query("""
        SELECT hc.* FROM habit_completions hc
        INNER JOIN habits h ON h.id = hc.habitId
        WHERE h.userId = :userId AND hc.date = :date
    """)
    suspend fun getTodayCompletions(userId: String, date: String): List<HabitCompletionEntity>
    
    @Query("""
        SELECT COUNT(*) FROM habit_completions hc
        INNER JOIN habits h ON h.id = hc.habitId
        WHERE h.userId = :userId AND hc.date = :date
    """)
    suspend fun getTodayCompletionsCount(userId: String, date: String): Int
    
    @Query("""
        SELECT hc.* FROM habit_completions hc
        INNER JOIN habits h ON h.id = hc.habitId
        WHERE h.userId = :userId AND hc.date = :date
    """)
    fun getTodayCompletionsFlow(userId: String, date: String): Flow<List<HabitCompletionEntity>>
    
    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId")
    suspend fun getTotalCompletionsCount(habitId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitCompletion(completion: HabitCompletionEntity)
    
    @Update
    suspend fun updateHabitCompletion(completion: HabitCompletionEntity)
    
    @Delete
    suspend fun deleteHabitCompletion(completion: HabitCompletionEntity)
    
    @Query("DELETE FROM habit_completions WHERE habitId = :habitId")
    suspend fun deleteAllHabitCompletions(habitId: String)
    
    @Query("DELETE FROM habit_completions WHERE habitId IN (SELECT id FROM habits WHERE userId = :userId)")
    suspend fun deleteAllUserHabitCompletions(userId: String)
}