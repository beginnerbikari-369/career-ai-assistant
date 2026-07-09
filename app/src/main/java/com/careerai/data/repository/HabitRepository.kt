package com.careerai.data.repository

import com.careerai.data.database.dao.HabitDao
import com.careerai.data.database.entities.HabitEntity
import com.careerai.data.database.entities.HabitCompletionEntity
import com.careerai.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao
) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    fun getActiveHabitsFlow(userId: String): Flow<List<Habit>> {
        return habitDao.getActiveHabitsFlow(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    suspend fun createHabit(
        userId: String,
        name: String,
        description: String?,
        category: HabitCategory,
        frequency: HabitFrequency,
        targetCount: Int = 1,
        duration: Int? = null,
        reminderTime: String? = null,
        difficulty: HabitDifficulty = HabitDifficulty.MEDIUM
    ): Result<String> {
        return try {
            val habitId = UUID.randomUUID().toString()
            val timestamp = System.currentTimeMillis()
            
            val habit = HabitEntity(
                id = habitId,
                userId = userId,
                name = name,
                description = description,
                category = category.name.lowercase(),
                color = getDefaultColorForCategory(category),
                icon = getDefaultIconForCategory(category),
                frequency = frequency.name.lowercase(),
                targetCount = targetCount,
                duration = duration,
                reminderTime = reminderTime,
                difficulty = difficulty.name.lowercase(),
                createdAt = timestamp,
                updatedAt = timestamp
            )
            
            habitDao.insertHabit(habit)
            Result.success(habitId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun completeHabit(habitId: String, date: Date = Date()): Result<Unit> {
        return try {
            val dateString = dateFormat.format(date)
            val timestamp = System.currentTimeMillis()
            
            // Check if already completed today
            val existingCompletion = habitDao.getHabitCompletion(habitId, dateString)
            
            if (existingCompletion == null) {
                val completionId = UUID.randomUUID().toString()
                val completion = HabitCompletionEntity(
                    id = completionId,
                    habitId = habitId,
                    date = dateString,
                    completedAt = timestamp
                )
                
                habitDao.insertHabitCompletion(completion)
                
                // Update habit statistics
                val habit = habitDao.getHabitById(habitId)
                if (habit != null) {
                    val newStreak = calculateStreak(habitId, dateString)
                    val newLongestStreak = maxOf(habit.longestStreak, newStreak)
                    habitDao.updateHabitStats(habitId, newStreak, newLongestStreak, timestamp)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTodayCompletionsCount(userId: String): Result<Int> {
        return try {
            val today = dateFormat.format(Date())
            val count = habitDao.getTodayCompletionsCount(userId, today)
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun calculateStreak(habitId: String, currentDate: String): Int {
        // Simple streak calculation - can be enhanced
        val completions = habitDao.getHabitCompletionsInRange(
            habitId, 
            getDateDaysAgo(30), 
            currentDate
        )
        
        // Count consecutive days from today backwards
        var streak = 0
        var checkDate = currentDate
        
        while (completions.any { it.date == checkDate }) {
            streak++
            checkDate = getPreviousDay(checkDate)
        }
        
        return streak
    }
    
    private fun getDefaultColorForCategory(category: HabitCategory): String {
        return when (category) {
            HabitCategory.HEALTH -> "#4CAF50"
            HabitCategory.PRODUCTIVITY -> "#2196F3"
            HabitCategory.LEARNING -> "#FF9800"
            HabitCategory.WELLNESS -> "#9C27B0"
            HabitCategory.CAREER -> "#F44336"
            else -> "#607D8B"
        }
    }
    
    private fun getDefaultIconForCategory(category: HabitCategory): String {
        return when (category) {
            HabitCategory.HEALTH -> "favorite"
            HabitCategory.PRODUCTIVITY -> "work"
            HabitCategory.LEARNING -> "school"
            HabitCategory.WELLNESS -> "spa"
            HabitCategory.CAREER -> "trending_up"
            HabitCategory.SOCIAL -> "people"
            HabitCategory.FINANCIAL -> "account_balance_wallet"
            HabitCategory.CREATIVITY -> "palette"
            HabitCategory.OTHER -> "star"
        }
    }
    
    private fun getDateDaysAgo(days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        return dateFormat.format(calendar.time)
    }
    
    private fun getPreviousDay(dateString: String): String {
        val date = dateFormat.parse(dateString) ?: return dateString
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(calendar.time)
    }
}

// Domain mapping
private fun HabitEntity.toDomain(): Habit {
    return Habit(
        id = id,
        userId = userId,
        name = name,
        description = description,
        category = HabitCategory.valueOf(category.uppercase()),
        color = color,
        icon = icon,
        frequency = HabitFrequency.valueOf(frequency.uppercase()),
        targetCount = targetCount,
        duration = duration,
        reminderTime = reminderTime,
        isReminderEnabled = isReminderEnabled,
        streakCount = streakCount,
        longestStreak = longestStreak,
        totalCompletions = totalCompletions,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isActive = isActive,
        isArchived = isArchived,
        difficulty = HabitDifficulty.valueOf(difficulty.uppercase())
    )
}