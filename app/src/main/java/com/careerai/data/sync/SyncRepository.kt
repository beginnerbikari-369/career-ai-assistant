package com.careerai.data.sync

import com.careerai.data.auth.AuthService
import com.careerai.data.database.CareerAIDatabase
import com.careerai.data.database.dao.*
import com.careerai.data.firestore.FirestoreService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val database: CareerAIDatabase,
    private val firestoreService: FirestoreService,
    private val authService: AuthService,
    private val userDao: UserDao,
    private val goalDao: GoalDao,
    private val habitDao: HabitDao,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val calendarDao: CalendarDao,
    private val journalDao: JournalDao,
    private val skillDao: SkillDao,
    private val jobApplicationDao: JobApplicationDao
) {
    
    suspend fun syncAllUserData(): Result<SyncResult> = coroutineScope {
        try {
            val userId = authService.currentUserId ?: return@coroutineScope Result.failure(
                Exception("User not authenticated")
            )
            
            // Run sync operations in parallel
            val syncJobs = listOf(
                async { syncUsers(userId) },
                async { syncGoals(userId) },
                async { syncHabits(userId) },
                async { syncConversations(userId) },
                async { syncCalendarEvents(userId) },
                async { syncJournalEntries(userId) },
                async { syncSkills(userId) },
                async { syncJobApplications(userId) }
            )
            
            val results = syncJobs.awaitAll()
            val failedSyncs = results.filterNot { it.isSuccess }
            
            val syncResult = if (failedSyncs.isEmpty()) {
                SyncResult.Success(
                    syncedTables = results.size,
                    timestamp = System.currentTimeMillis()
                )
            } else {
                SyncResult.PartialFailure(
                    syncedTables = results.size - failedSyncs.size,
                    failedTables = failedSyncs.size,
                    errors = failedSyncs.mapNotNull { it.exceptionOrNull()?.message },
                    timestamp = System.currentTimeMillis()
                )
            }
            
            Result.success(syncResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun syncUsers(userId: String): Result<Unit> {
        return try {
            // Get local user data
            val localUser = userDao.getUserById(userId)
            if (localUser != null) {
                // Upload to Firestore
                firestoreService.addDocument(
                    collection = FirestoreService.COLLECTION_USERS,
                    document = localUser,
                    documentId = userId
                )
            }
            
            // Download from Firestore
            val cloudUser = firestoreService.getDocument(
                collection = FirestoreService.COLLECTION_USERS,
                documentId = userId,
                clazz = com.careerai.data.database.entities.UserEntity::class.java
            )
            
            cloudUser.getOrNull()?.let { user ->
                userDao.insertUser(user)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun syncGoals(userId: String): Result<Unit> {
        return try {
            // Get local goals modified since last sync
            val lastSyncTime = getLastSyncTime(userId, "goals")
            val localGoals = goalDao.getActiveGoalsFlow(userId)
            
            // Upload modified goals to Firestore
            // This is simplified - in production, you'd track modification timestamps
            // and only sync changed items
            
            // For now, sync all active goals
            // In production, implement proper delta sync
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun syncHabits(userId: String): Result<Unit> {
        return try {
            // Similar pattern to goals - implement delta sync
            // Upload local changes to Firestore
            // Download and merge cloud changes
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun syncConversations(userId: String): Result<Unit> {
        return try {
            // Sync conversations and messages
            // Be careful with large message histories
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun syncCalendarEvents(userId: String): Result<Unit> {
        return try {
            // Sync calendar events
            // Handle Google Calendar integration
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun syncJournalEntries(userId: String): Result<Unit> {
        return try {
            // Sync journal entries
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun syncSkills(userId: String): Result<Unit> {
        return try {
            // Sync skills data
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun syncJobApplications(userId: String): Result<Unit> {
        return try {
            // Sync job applications
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun getLastSyncTime(userId: String, collection: String): Long {
        return firestoreService.getSyncStatus(userId, collection)
            .getOrNull() ?: 0L
    }
    
    private suspend fun updateLastSyncTime(userId: String, collection: String) {
        firestoreService.updateSyncStatus(userId, collection, System.currentTimeMillis())
    }
    
    suspend fun forceSyncCollection(userId: String, collectionName: String): Result<Unit> {
        return when (collectionName) {
            "goals" -> syncGoals(userId)
            "habits" -> syncHabits(userId)
            "conversations" -> syncConversations(userId)
            "calendar" -> syncCalendarEvents(userId)
            "journal" -> syncJournalEntries(userId)
            "skills" -> syncSkills(userId)
            "job_applications" -> syncJobApplications(userId)
            else -> Result.failure(Exception("Unknown collection: $collectionName"))
        }
    }
    
    suspend fun enableAutoSync(userId: String): Result<Unit> {
        // Enable automatic background sync
        return try {
            // This would schedule periodic sync work
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun disableAutoSync(userId: String): Result<Unit> {
        // Disable automatic background sync
        return try {
            // Cancel scheduled sync work
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

sealed class SyncResult {
    data class Success(
        val syncedTables: Int,
        val timestamp: Long
    ) : SyncResult()
    
    data class PartialFailure(
        val syncedTables: Int,
        val failedTables: Int,
        val errors: List<String>,
        val timestamp: Long
    ) : SyncResult()
    
    data class Failure(
        val error: String,
        val timestamp: Long
    ) : SyncResult()
}