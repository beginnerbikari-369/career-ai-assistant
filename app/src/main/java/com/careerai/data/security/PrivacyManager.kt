package com.careerai.data.security

import com.careerai.data.database.CareerAIDatabase
import com.careerai.data.firestore.FirestoreService
import com.careerai.data.auth.AuthService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrivacyManager @Inject constructor(
    private val database: CareerAIDatabase,
    private val firestoreService: FirestoreService,
    private val authService: AuthService,
    private val encryptionService: EncryptionService
) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    
    suspend fun exportUserData(): Result<File> = withContext(Dispatchers.IO) {
        try {
            val userId = authService.currentUserId 
                ?: return@withContext Result.failure(Exception("User not authenticated"))
            
            val exportData = collectUserData(userId)
            val exportJson = kotlinx.serialization.json.Json {
                prettyPrint = true
            }.encodeToString(
                kotlinx.serialization.serializer<UserDataExport>(),
                exportData
            )
            
            // Create export file
            val timestamp = dateFormat.format(Date())
            val fileName = "career_ai_export_$timestamp.json"
            val exportFile = File(android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS
            ), fileName)
            
            exportFile.writeText(exportJson)
            
            Result.success(exportFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun collectUserData(userId: String): UserDataExport {
        return UserDataExport(
            exportDate = System.currentTimeMillis(),
            userId = userId,
            user = database.userDao().getUserById(userId),
            goals = database.goalDao().getActiveGoalsFlow(userId).toString(), // Simplified for demo
            habits = database.habitDao().getActiveHabitsCount(userId),
            conversations = database.conversationDao().getActiveConversationsFlow(userId).toString(),
            calendarEvents = database.calendarDao().getEventsCountInRange(userId, 0, Long.MAX_VALUE),
            journalEntries = database.journalDao().getTotalEntriesCount(userId),
            skills = database.skillDao().getTotalSkillsCount(userId),
            jobApplications = database.jobApplicationDao().getTotalApplicationsCount(userId)
        )
    }
    
    suspend fun deleteAllUserData(): Result<Unit> {
        return try {
            val userId = authService.currentUserId 
                ?: return Result.failure(Exception("User not authenticated"))
            
            // Delete from local database
            deleteLocalUserData(userId)
            
            // Delete from cloud (Firestore)
            deleteCloudUserData(userId)
            
            // Clear secure storage
            encryptionService.clearAllSecureData()
            
            // Sign out user
            authService.signOut()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun deleteLocalUserData(userId: String) {
        database.userDao().deleteAllUserGoals(userId)
        database.habitDao().deleteAllUserHabits(userId)
        database.habitDao().deleteAllUserHabitCompletions(userId)
        database.conversationDao().deleteAllUserConversations(userId)
        database.messageDao().deleteAllUserMessages(userId)
        database.calendarDao().deleteAllUserEvents(userId)
        database.journalDao().deleteAllUserEntries(userId)
        database.skillDao().deleteAllUserSkills(userId)
        database.jobApplicationDao().deleteAllUserApplications(userId)
        
        // Finally delete user record
        val user = database.userDao().getUserById(userId)
        user?.let { database.userDao().deleteUser(it) }
    }
    
    private suspend fun deleteCloudUserData(userId: String) {
        // Delete user document and all subcollections from Firestore
        val collections = listOf(
            "goals",
            "habits", 
            "habit_completions",
            "conversations",
            "messages",
            "calendar_events",
            "journal_entries",
            "skills",
            "job_applications"
        )
        
        collections.forEach { collection ->
            // In a real implementation, you'd need to recursively delete subcollections
            // This is a simplified version
            firestoreService.deleteDocument("users/$userId/$collection", userId)
        }
        
        // Delete main user document
        firestoreService.deleteDocument(FirestoreService.COLLECTION_USERS, userId)
    }
    
    suspend fun getPrivacySettings(userId: String): PrivacySettings {
        // Get current privacy settings from database or create defaults
        return PrivacySettings(
            dataCollection = true,
            analyticsEnabled = true,
            cloudSyncEnabled = true,
            aiLearningEnabled = true,
            notificationsEnabled = true,
            locationTrackingEnabled = false,
            thirdPartyIntegrationsEnabled = true
        )
    }
    
    suspend fun updatePrivacySettings(userId: String, settings: PrivacySettings): Result<Unit> {
        return try {
            // Store privacy settings
            val settingsJson = kotlinx.serialization.json.Json.encodeToString(
                kotlinx.serialization.serializer<PrivacySettings>(),
                settings
            )
            
            encryptionService.storeSecureString("privacy_settings_$userId", settingsJson)
            
            // Apply settings changes
            applyPrivacySettings(userId, settings)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun applyPrivacySettings(userId: String, settings: PrivacySettings) {
        // Apply privacy settings changes
        if (!settings.cloudSyncEnabled) {
            // Disable cloud sync
            // Cancel sync workers
        }
        
        if (!settings.analyticsEnabled) {
            // Disable analytics collection
        }
        
        if (!settings.aiLearningEnabled) {
            // Disable AI model learning from user data
        }
        
        if (!settings.notificationsEnabled) {
            // Disable notifications
        }
    }
    
    suspend fun anonymizeUserData(userId: String): Result<Unit> {
        return try {
            // Replace personal identifiable information with anonymized data
            val user = database.userDao().getUserById(userId)
            user?.let { currentUser ->
                val anonymizedUser = currentUser.copy(
                    email = "anonymized_${UUID.randomUUID()}@example.com",
                    displayName = "Anonymous User",
                    profileImageUrl = null
                )
                database.userDao().updateUser(anonymizedUser)
            }
            
            // Anonymize journal entries
            val journalEntries = database.journalDao().getEntriesInDateRange(
                userId, "1970-01-01", "2100-12-31"
            )
            
            journalEntries.forEach { entry ->
                val anonymizedEntry = entry.copy(
                    content = "[Content anonymized]",
                    highlights = null,
                    challenges = null,
                    learnings = null,
                    gratitude = null
                )
                database.journalDao().updateEntry(anonymizedEntry)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getDataUsageReport(userId: String): DataUsageReport {
        val totalGoals = database.goalDao().getActiveGoalsCount(userId) + 
                        database.goalDao().getCompletedGoalsCount(userId)
        val totalHabits = database.habitDao().getActiveHabitsCount(userId)
        val totalConversations = database.conversationDao().getActiveConversationsFlow(userId).toString().length // Simplified
        val totalJournalEntries = database.journalDao().getTotalEntriesCount(userId)
        val totalCalendarEvents = database.calendarDao().getEventsCountInRange(userId, 0, Long.MAX_VALUE)
        val totalSkills = database.skillDao().getTotalSkillsCount(userId)
        val totalJobApplications = database.jobApplicationDao().getTotalApplicationsCount(userId)
        
        return DataUsageReport(
            totalGoals = totalGoals,
            totalHabits = totalHabits,
            totalConversations = totalConversations,
            totalJournalEntries = totalJournalEntries,
            totalCalendarEvents = totalCalendarEvents,
            totalSkills = totalSkills,
            totalJobApplications = totalJobApplications,
            estimatedStorageSize = calculateStorageSize(userId),
            lastSyncDate = System.currentTimeMillis() // Simplified
        )
    }
    
    private suspend fun calculateStorageSize(userId: String): Long {
        // Estimate storage size used by user data
        // This is a simplified calculation
        return 1024 * 1024 // 1MB placeholder
    }
}

@kotlinx.serialization.Serializable
data class UserDataExport(
    val exportDate: Long,
    val userId: String,
    val user: com.careerai.data.database.entities.UserEntity?,
    val goals: String, // Simplified for demo
    val habits: Int,
    val conversations: String,
    val calendarEvents: Int,
    val journalEntries: Int,
    val skills: Int,
    val jobApplications: Int
)

@kotlinx.serialization.Serializable
data class PrivacySettings(
    val dataCollection: Boolean = true,
    val analyticsEnabled: Boolean = true,
    val cloudSyncEnabled: Boolean = true,
    val aiLearningEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val locationTrackingEnabled: Boolean = false,
    val thirdPartyIntegrationsEnabled: Boolean = true
)

data class DataUsageReport(
    val totalGoals: Int,
    val totalHabits: Int,
    val totalConversations: Int,
    val totalJournalEntries: Int,
    val totalCalendarEvents: Int,
    val totalSkills: Int,
    val totalJobApplications: Int,
    val estimatedStorageSize: Long,
    val lastSyncDate: Long
)