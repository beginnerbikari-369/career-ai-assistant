package com.careerai

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CareerAIApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize WorkManager
        val workManagerConfiguration = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
        
        WorkManager.initialize(this, workManagerConfiguration)
    }
}