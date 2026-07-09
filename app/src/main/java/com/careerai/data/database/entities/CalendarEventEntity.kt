package com.careerai.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "calendar_events")
@Serializable
data class CalendarEventEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val googleEventId: String?, // Google Calendar event ID if synced
    val title: String,
    val description: String?,
    val startTime: Long,
    val endTime: Long,
    val location: String?,
    val isAllDay: Boolean = false,
    val category: String?, // meeting, focus_time, break, personal, work
    val color: String?, // Hex color
    val reminderMinutes: String? = null, // JSON array of reminder minutes before event
    val attendees: String? = null, // JSON array of attendee emails
    val recurrenceRule: String? = null, // RRULE for recurring events
    val isFromGoogleCalendar: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val lastSyncedAt: Long? = null,
    val isDeleted: Boolean = false
)