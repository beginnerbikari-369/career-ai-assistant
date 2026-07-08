package com.careerai.di

import androidx.work.WorkManager
import com.careerai.data.notifications.NotificationScheduler
import com.careerai.data.repository.*
import com.careerai.data.security.EncryptionService
import com.careerai.data.security.PrivacyManager
import com.careerai.data.sync.SyncRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideNotificationScheduler(workManager: WorkManager): NotificationScheduler {
        return NotificationScheduler(workManager)
    }
    
    @Provides
    @Singleton
    fun provideWorkManager(
        @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context
    ): WorkManager {
        return WorkManager.getInstance(context)
    }
}