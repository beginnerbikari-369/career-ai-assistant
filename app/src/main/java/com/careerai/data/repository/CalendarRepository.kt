package com.careerai.data.repository

import com.careerai.data.calendar.CalendarEventData
import com.careerai.data.calendar.GoogleCalendarService
import com.careerai.data.database.dao.CalendarDao
import com.careerai.data.database.entities.CalendarEventEntity
import com.careerai.domain.model.CalendarEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepository @Inject constructor(
    private val calendarDao: CalendarDao,
    private val googleCalendarService: GoogleCalendarService
) {
    
    fun getEventsFlow(userId: String): Flow<List<CalendarEvent>> {
        return calendarDao.getEventsFlow(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    fun getEventsByDateRangeFlow(
        userId: String, 
        startTime: Long, 
        endTime: Long
    ): Flow<List<CalendarEvent>> {
        return calendarDao.getEventsByDateRangeFlow(userId, startTime, endTime)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    suspend fun syncWithGoogleCalendar(
        userId: String,
        startTime: Long,
        endTime: Long
    ): Result<Unit> {
        return try {
            // Authenticate with Google Calendar
            val authResult = googleCalendarService.authenticateUser()
            if (authResult.isFailure) {
                return Result.failure(authResult.exceptionOrNull() ?: Exception("Authentication failed"))
            }
            
            // Fetch events from Google Calendar
            val eventsResult = googleCalendarService.fetchEvents(startTime, endTime)
            eventsResult.fold(
                onSuccess = { googleEvents ->
                    // Convert and save to local database
                    val entities = googleEvents.map { eventData ->
                        eventData.toEntity(userId)
                    }
                    
                    calendarDao.insertEvents(entities)
                    
                    // Update last sync time
                    calendarDao.updateLastSyncTime(System.currentTimeMillis())
                    
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createEvent(
        userId: String,
        title: String,
        description: String? = null,
        startTime: Long,
        endTime: Long,
        location: String? = null,
        isAllDay: Boolean = false,
        reminderMinutes: List<Int> = listOf(15),
        syncToGoogle: Boolean = true
    ): Result<String> {
        return try {
            val eventId = UUID.randomUUID().toString()
            val timestamp = System.currentTimeMillis()
            
            var googleEventId: String? = null
            
            // Create in Google Calendar if sync enabled and authenticated
            if (syncToGoogle && googleCalendarService.isAuthenticated()) {
                val eventData = CalendarEventData(
                    id = eventId,
                    title = title,
                    description = description,
                    startTime = startTime,
                    endTime = endTime,
                    location = location,
                    isAllDay = isAllDay,
                    reminderMinutes = reminderMinutes
                )
                
                val createResult = googleCalendarService.createEvent(eventData)
                createResult.fold(
                    onSuccess = { gEventId -> googleEventId = gEventId },
                    onFailure = { /* Log error but continue with local creation */ }
                )
            }
            
            // Save to local database
            val entity = CalendarEventEntity(
                id = eventId,
                userId = userId,
                googleEventId = googleEventId,
                title = title,
                description = description,
                startTime = startTime,
                endTime = endTime,
                location = location,
                isAllDay = isAllDay,
                reminderMinutes = Json.encodeToString(reminderMinutes),
                isFromGoogleCalendar = googleEventId != null,
                createdAt = timestamp,
                updatedAt = timestamp,
                lastSyncedAt = if (googleEventId != null) timestamp else null
            )
            
            calendarDao.insertEvent(entity)
            Result.success(eventId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateEvent(event: CalendarEvent): Result<Unit> {
        return try {
            val entity = event.toEntity()
            calendarDao.updateEvent(entity)
            
            // Update in Google Calendar if it's synced
            if (event.isFromGoogleCalendar && event.googleEventId != null) {
                val eventData = CalendarEventData(
                    id = event.id,
                    googleEventId = event.googleEventId,
                    title = event.title,
                    description = event.description,
                    startTime = event.startTime,
                    endTime = event.endTime,
                    location = event.location,
                    isAllDay = event.isAllDay,
                    reminderMinutes = event.reminderMinutes
                )
                
                googleCalendarService.updateEvent(eventData)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteEvent(eventId: String): Result<Unit> {
        return try {
            val event = calendarDao.getEventById(eventId)
            if (event != null) {
                // Delete from Google Calendar if synced
                if (event.isFromGoogleCalendar && event.googleEventId != null) {
                    googleCalendarService.deleteEvent(event.googleEventId)
                }
                
                // Mark as deleted in local database
                calendarDao.markEventAsDeleted(eventId, System.currentTimeMillis())
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUpcomingEvents(userId: String, limit: Int = 10): Result<List<CalendarEvent>> {
        return try {
            val currentTime = System.currentTimeMillis()
            val events = calendarDao.getUpcomingEvents(userId, currentTime, limit)
            Result.success(events.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun authenticateGoogleCalendar(): Result<Unit> {
        return googleCalendarService.authenticateUser().map { Unit }
    }
    
    fun isGoogleCalendarAuthenticated(): Boolean {
        return googleCalendarService.isAuthenticated()
    }
}

// Extension functions for domain mapping
private fun CalendarEventEntity.toDomain(): CalendarEvent {
    val reminderList = reminderMinutes?.let {
        try {
            Json.decodeFromString<List<Int>>(it)
        } catch (e: Exception) {
            emptyList()
        }
    } ?: emptyList()
    
    val attendeeList = attendees?.let {
        try {
            Json.decodeFromString<List<String>>(it)
        } catch (e: Exception) {
            emptyList()
        }
    } ?: emptyList()
    
    return CalendarEvent(
        id = id,
        userId = userId,
        googleEventId = googleEventId,
        title = title,
        description = description,
        startTime = startTime,
        endTime = endTime,
        location = location,
        isAllDay = isAllDay,
        category = category,
        color = color,
        reminderMinutes = reminderList,
        attendees = attendeeList,
        recurrenceRule = recurrenceRule,
        isFromGoogleCalendar = isFromGoogleCalendar,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}

private fun CalendarEvent.toEntity(): CalendarEventEntity {
    return CalendarEventEntity(
        id = id,
        userId = userId,
        googleEventId = googleEventId,
        title = title,
        description = description,
        startTime = startTime,
        endTime = endTime,
        location = location,
        isAllDay = isAllDay,
        category = category,
        color = color,
        reminderMinutes = if (reminderMinutes.isNotEmpty()) Json.encodeToString(reminderMinutes) else null,
        attendees = if (attendees.isNotEmpty()) Json.encodeToString(attendees) else null,
        recurrenceRule = recurrenceRule,
        isFromGoogleCalendar = isFromGoogleCalendar,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis(),
        lastSyncedAt = lastSyncedAt
    )
}

private fun CalendarEventData.toEntity(userId: String): CalendarEventEntity {
    return CalendarEventEntity(
        id = id,
        userId = userId,
        googleEventId = googleEventId,
        title = title,
        description = description,
        startTime = startTime,
        endTime = endTime,
        location = location,
        isAllDay = isAllDay,
        reminderMinutes = if (reminderMinutes.isNotEmpty()) Json.encodeToString(reminderMinutes) else null,
        attendees = if (attendees.isNotEmpty()) Json.encodeToString(attendees) else null,
        recurrenceRule = recurrenceRule,
        isFromGoogleCalendar = true,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        lastSyncedAt = lastSyncedAt
    )
}

// Domain model for CalendarEvent
data class CalendarEvent(
    val id: String,
    val userId: String,
    val googleEventId: String? = null,
    val title: String,
    val description: String? = null,
    val startTime: Long,
    val endTime: Long,
    val location: String? = null,
    val isAllDay: Boolean = false,
    val category: String? = null,
    val color: String? = null,
    val reminderMinutes: List<Int> = emptyList(),
    val attendees: List<String> = emptyList(),
    val recurrenceRule: String? = null,
    val isFromGoogleCalendar: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val lastSyncedAt: Long? = null
)