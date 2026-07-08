package com.careerai.repository

import com.careerai.data.database.dao.GoalDao
import com.careerai.data.database.entities.GoalEntity
import com.careerai.data.repository.GoalRepository
import com.careerai.domain.model.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class GoalRepositoryTest {
    
    @Mock
    private lateinit var goalDao: GoalDao
    
    private lateinit var repository: GoalRepository
    
    private val testUserId = "test_user_123"
    private val testGoalId = UUID.randomUUID().toString()
    
    @Before
    fun setup() {
        repository = GoalRepository(goalDao)
    }
    
    @Test
    fun `createGoal should return success with valid data`() = runTest {
        // Given
        val title = "Learn Kotlin"
        val description = "Master Kotlin programming"
        val category = GoalCategory.SKILL
        val priority = Priority.HIGH
        val type = GoalType.LONG_TERM
        
        // When
        val result = repository.createGoal(
            userId = testUserId,
            title = title,
            description = description,
            category = category,
            priority = priority,
            type = type
        )
        
        // Then
        assertTrue(result.isSuccess)
        verify(goalDao).insertGoal(any())
    }
    
    @Test
    fun `updateGoalProgress should complete goal when progress reaches 100`() = runTest {
        // Given
        val progress = 100
        
        // When
        val result = repository.updateGoalProgress(testGoalId, progress)
        
        // Then
        assertTrue(result.isSuccess)
        verify(goalDao).updateProgress(testGoalId, progress, any())
        verify(goalDao).updateCompletionStatus(eq(testGoalId), eq(true), any(), eq(100), any())
    }
    
    @Test
    fun `getGoalStatistics should return correct statistics`() = runTest {
        // Given
        whenever(goalDao.getActiveGoalsCount(testUserId)).thenReturn(5)
        whenever(goalDao.getCompletedGoalsCount(testUserId)).thenReturn(3)
        whenever(goalDao.getAverageProgress(testUserId)).thenReturn(75.0)
        
        // When
        val result = repository.getGoalStatistics(testUserId)
        
        // Then
        assertTrue(result.isSuccess)
        val stats = result.getOrNull()
        assertNotNull(stats)
        assertEquals(5, stats?.totalGoals)
        assertEquals(3, stats?.completedGoals)
        assertEquals(75, stats?.averageProgress)
    }
    
    @Test
    fun `getOverdueGoals should return goals past target date`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val overdueGoals = listOf(
            createTestGoalEntity(testGoalId, "Overdue Goal", currentTime - 86400000) // 1 day ago
        )
        whenever(goalDao.getOverdueGoals(testUserId, currentTime)).thenReturn(overdueGoals)
        
        // When
        val result = repository.getOverdueGoals(testUserId)
        
        // Then
        assertTrue(result.isSuccess)
        val goals = result.getOrNull()
        assertNotNull(goals)
        assertEquals(1, goals?.size)
        assertEquals("Overdue Goal", goals?.first()?.title)
    }
    
    private fun createTestGoalEntity(id: String, title: String, targetDate: Long): GoalEntity {
        return GoalEntity(
            id = id,
            userId = testUserId,
            title = title,
            description = null,
            category = "skill",
            priority = "high",
            type = "long_term",
            targetDate = targetDate,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}