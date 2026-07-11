package com.careerai

import android.app.Application
import com.careerai.data.api.ApiKeyManager
import com.careerai.data.notifications.SmartNotificationService
import com.careerai.data.seeding.DataSeedingService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class CareerAIApplication : Application() {
    
    @Inject
    lateinit var apiKeyManager: ApiKeyManager
    
    @Inject
    lateinit var notificationService: SmartNotificationService
    
    @Inject
    lateinit var dataSeedingService: DataSeedingService
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize API keys from BuildConfig
        apiKeyManager.initializeApiKeys()
        
        // Initialize smart notifications for the current user
        // In a real app, this would be done after user login
        val userId = "dummy_user_id" // This should come from user session
        
        applicationScope.launch {
            try {
                // Seed sample data on first run
                dataSeedingService.seedSampleData(userId)
                
                // Set up smart notifications
                notificationService.scheduleSmartNotifications(userId)
            } catch (e: Exception) {
                // Handle initialization errors gracefully
            }
        }
    }
}