package com.careerai.di

import android.content.Context
import androidx.room.Room
import com.careerai.data.database.CareerAIDatabase
import com.careerai.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CareerAIDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            CareerAIDatabase::class.java,
            CareerAIDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideUserDao(database: CareerAIDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    fun provideConversationDao(database: CareerAIDatabase): ConversationDao {
        return database.conversationDao()
    }
    
    @Provides
    fun provideMessageDao(database: CareerAIDatabase): MessageDao {
        return database.messageDao()
    }
    
    @Provides
    fun provideGoalDao(database: CareerAIDatabase): GoalDao {
        return database.goalDao()
    }
    
    @Provides
    fun provideHabitDao(database: CareerAIDatabase): HabitDao {
        return database.habitDao()
    }
    
    @Provides
    fun provideCalendarDao(database: CareerAIDatabase): CalendarDao {
        return database.calendarDao()
    }
    
    @Provides
    fun provideJournalDao(database: CareerAIDatabase): JournalDao {
        return database.journalDao()
    }
    
    @Provides
    fun provideSkillDao(database: CareerAIDatabase): SkillDao {
        return database.skillDao()
    }
    
    @Provides
    fun provideJobApplicationDao(database: CareerAIDatabase): JobApplicationDao {
        return database.jobApplicationDao()
    }
}