package com.careerai.data.database.dao

import androidx.room.*
import com.careerai.data.database.entities.CalendarEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {
    
    @Query("SELECT * FROM calendar_events WHERE userId = :userId AND isDeleted = 0 ORDER BY startTime ASC")
    fun getEventsFlow(userId: String): Flow<List<CalendarEventEntity>>
    
    @Query("SELECT * FROM calendar_events WHERE userId = :userId AND startTime BETWEEN :startTime AND :endTime AND isDeleted = 0 ORDER BY startTime ASC")
    fun getEventsByDateRangeFlow(userId: String, startTime: Long, endTime: Long): Flow<List<CalendarEventEntity>>
    
    @Query("SELECT * FROM calendar_events WHERE id = :eventId")
    suspend fun getEventById(eventId: String): CalendarEventEntity?
    
    @Query("SELECT * FROM calendar_events WHERE googleEventId = :googleEventId")
    suspend fun getEventByGoogleId(googleEventId: String): CalendarEventEntity?
    
    @Query("SELECT * FROM calendar_events WHERE userId = :userId AND startTime >= :currentTime AND isDeleted = 0 ORDER BY startTime ASC LIMIT :limit")
    suspend fun getUpcomingEvents(userId: String, currentTime: Long, limit: Int = 10): List<CalendarEventEntity>
    
    @Query("SELECT * FROM calendar_events WHERE userId = :userId AND category = :category AND isDeleted = 0 ORDER BY startTime ASC")
    fun getEventsByCategoryFlow(userId: String, category: String): Flow<List<CalendarEventEntity>>
    
    @Query("SELECT * FROM calendar_events WHERE userId = :userId AND isFromGoogleCalendar = 1 AND isDeleted = 0")
    suspend fun getGoogleCalendarEvents(userId: String): List<CalendarEventEntity>
    
    @Query("SELECT COUNT(*) FROM calendar_events WHERE userId = :userId AND startTime BETWEEN :startTime AND :endTime AND isDeleted = 0")
    suspend fun getEventsCountInRange(userId: String, startTime: Long, endTime: Long): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<CalendarEventEntity>)
    
    @Update
    suspend fun updateEvent(event: CalendarEventEntity)
    
    @Delete
    suspend fun deleteEvent(event: CalendarEventEntity)
    
    @Query("UPDATE calendar_events SET isDeleted = 1, updatedAt = :timestamp WHERE id = :eventId")
    suspend fun markEventAsDeleted(eventId: String, timestamp: Long)
    
    @Query("UPDATE calendar_events SET lastSyncedAt = :syncTime WHERE googleEventId IS NOT NULL")
    suspend fun updateLastSyncTime(syncTime: Long)
    
    @Query("DELETE FROM calendar_events WHERE userId = :userId")
    suspend fun deleteAllUserEvents(userId: String)
    
    @Query("DELETE FROM calendar_events WHERE isDeleted = 1 AND updatedAt < :cutoffTime")
    suspend fun permanentlyDeleteOldEvents(cutoffTime: Long)
}