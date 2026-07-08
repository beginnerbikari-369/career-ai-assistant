package com.careerai.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.careerai.data.database.CareerAIDatabase
import com.careerai.data.database.dao.GoalDao
import com.careerai.data.database.entities.GoalEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class GoalDaoTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var database: CareerAIDatabase
    private lateinit var goalDao: GoalDao
    
    private val testUserId = "test_user_123"
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CareerAIDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        
        goalDao = database.goalDao()
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun insertGoalAndRetrieve() = runTest {
        // Given
        val goal = createTestGoal()
        
        // When
        goalDao.insertGoal(goal)
        val retrievedGoal = goalDao.getGoalById(goal.id)
        
        // Then
        assertNotNull(retrievedGoal)
        assertEquals(goal.id, retrievedGoal?.id)
        assertEquals(goal.title, retrievedGoal?.title)
        assertEquals(goal.userId, retrievedGoal?.userId)
    }
    
    @Test
    fun getActiveGoalsFlow() = runTest {
        // Given
        val activeGoal = createTestGoal()
        val archivedGoal = createTestGoal().copy(id = UUID.randomUUID().toString(), isArchived = true)
        
        goalDao.insertGoal(activeGoal)
        goalDao.insertGoal(archivedGoal)
        
        // When
        val activeGoals = goalDao.getActiveGoalsFlow(testUserId).first()
        
        // Then
        assertEquals(1, activeGoals.size)
        assertEquals(activeGoal.id, activeGoals.first().id)
        assertFalse(activeGoals.first().isArchived)
    }
    
    @Test
    fun updateProgress() = runTest {
        // Given
        val goal = createTestGoal()
        goalDao.insertGoal(goal)
        
        // When
        val newProgress = 75
        goalDao.updateProgress(goal.id, newProgress, System.currentTimeMillis())
        val updatedGoal = goalDao.getGoalById(goal.id)
        
        // Then
        assertNotNull(updatedGoal)
        assertEquals(newProgress, updatedGoal?.progress)
    }
    
    @Test
    fun updateCompletionStatus() = runTest {
        // Given
        val goal = createTestGoal()
        goalDao.insertGoal(goal)
        
        // When
        val completedAt = System.currentTimeMillis()
        goalDao.updateCompletionStatus(goal.id, true, completedAt, 100, completedAt)
        val completedGoal = goalDao.getGoalById(goal.id)
        
        // Then
        assertNotNull(completedGoal)
        assertTrue(completedGoal?.isCompleted ?: false)
        assertEquals(100, completedGoal?.progress)
        assertEquals(completedAt, completedGoal?.completedAt)
    }
    
    @Test
    fun getIncompleteGoals() = runTest {
        // Given
        val incompleteGoal = createTestGoal()
        val completedGoal = createTestGoal().copy(
            id = UUID.randomUUID().toString(),
            isCompleted = true
        )
        
        goalDao.insertGoal(incompleteGoal)
        goalDao.insertGoal(completedGoal)
        
        // When
        val incompleteGoals = goalDao.getIncompleteGoalsFlow(testUserId).first()
        
        // Then
        assertEquals(1, incompleteGoals.size)
        assertEquals(incompleteGoal.id, incompleteGoals.first().id)
        assertFalse(incompleteGoals.first().isCompleted)
    }
    
    @Test
    fun getGoalsByCategory() = runTest {
        // Given
        val careerGoal = createTestGoal().copy(category = "career")
        val skillGoal = createTestGoal().copy(
            id = UUID.randomUUID().toString(),
            category = "skill"
        )
        
        goalDao.insertGoal(careerGoal)
        goalDao.insertGoal(skillGoal)
        
        // When
        val careerGoals = goalDao.getGoalsByCategoryFlow(testUserId, "career").first()
        
        // Then
        assertEquals(1, careerGoals.size)
        assertEquals(careerGoal.id, careerGoals.first().id)
        assertEquals("career", careerGoals.first().category)
    }
    
    @Test
    fun getGoalStatistics() = runTest {
        // Given
        val completedGoal = createTestGoal().copy(isCompleted = true)
        val activeGoal1 = createTestGoal().copy(
            id = UUID.randomUUID().toString(),
            progress = 50
        )
        val activeGoal2 = createTestGoal().copy(
            id = UUID.randomUUID().toString(),
            progress = 80
        )
        
        goalDao.insertGoal(completedGoal)
        goalDao.insertGoal(activeGoal1)
        goalDao.insertGoal(activeGoal2)
        
        // When
        val completedCount = goalDao.getCompletedGoalsCount(testUserId)
        val activeCount = goalDao.getActiveGoalsCount(testUserId)
        val averageProgress = goalDao.getAverageProgress(testUserId)
        
        // Then
        assertEquals(1, completedCount)
        assertEquals(2, activeCount)
        assertEquals(65.0, averageProgress, 0.1) // (50 + 80) / 2 = 65
    }
    
    private fun createTestGoal(
        id: String = UUID.randomUUID().toString(),
        title: String = "Test Goal"
    ): GoalEntity {
        return GoalEntity(
            id = id,
            userId = testUserId,
            title = title,
            description = "Test Description",
            category = "career",
            priority = "high",
            type = "long_term",
            targetDate = System.currentTimeMillis() + 86400000, // Tomorrow
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}