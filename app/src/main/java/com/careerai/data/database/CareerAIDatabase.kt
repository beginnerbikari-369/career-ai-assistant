package com.careerai.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.careerai.data.database.dao.*
import com.careerai.data.database.entities.*

@Database(
    entities = [
        UserEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        GoalEntity::class,
        HabitEntity::class,
        HabitCompletionEntity::class,
        CalendarEventEntity::class,
        JournalEntryEntity::class,
        SkillEntity::class,
        JobApplicationEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CareerAIDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun goalDao(): GoalDao
    abstract fun habitDao(): HabitDao
    abstract fun calendarDao(): CalendarDao
    abstract fun journalDao(): JournalDao
    abstract fun skillDao(): SkillDao
    abstract fun jobApplicationDao(): JobApplicationDao
    
    companion object {
        const val DATABASE_NAME = "career_ai_database"
        
        @Volatile
        private var INSTANCE: CareerAIDatabase? = null
        
        fun getDatabase(context: Context): CareerAIDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CareerAIDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}