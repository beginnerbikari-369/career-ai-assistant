package com.careerai.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class CalendarEvent(
    val id: String,
    val title: String,
    val description: String = "",
    val startTime: Long,
    val endTime: Long,
    val location: String = "",
    val isAllDay: Boolean = false,
    val category: EventCategory = EventCategory.OTHER,
    val color: String = "#2196F3", // Default blue
    val reminderMinutes: List<Int> = listOf(15), // Default 15 min reminder
    val recurrenceRule: String? = null, // RRULE for recurring events
    val isCareerRelated: Boolean = false,
    val attendees: List<String> = emptyList(),
    val googleEventId: String? = null, // Google Calendar event ID
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {
    
    val duration: Long
        get() = endTime - startTime
    
    val isUpcoming: Boolean
        get() = startTime > System.currentTimeMillis()
    
    val isOngoing: Boolean
        get() = System.currentTimeMillis() in startTime..endTime
    
    val isPast: Boolean
        get() = endTime < System.currentTimeMillis()
}

enum class EventCategory(val displayName: String, val color: String) {
    WORK("Work", "#FF5722"),
    MEETING("Meeting", "#2196F3"),  
    PERSONAL("Personal", "#4CAF50"),
    LEARNING("Learning", "#9C27B0"),
    HEALTH("Health", "#FF9800"),
    CAREER("Career", "#E91E63"),
    SOCIAL("Social", "#00BCD4"),
    OTHER("Other", "#607D8B")
}