package com.careerai.data.calendar

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.EventReminder
import com.careerai.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleCalendarService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var googleSignInClient: GoogleSignInClient? = null
    private var calendarService: Calendar? = null
    
    init {
        setupGoogleSignIn()
    }
    
    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(CalendarScopes.CALENDAR))
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }
    
    suspend fun authenticateUser(): Result<GoogleSignInAccount> {
        return try {
            val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(context)
            if (lastSignedInAccount != null && GoogleSignIn.hasPermissions(lastSignedInAccount, Scope(CalendarScopes.CALENDAR))) {
                setupCalendarService(lastSignedInAccount)
                Result.success(lastSignedInAccount)
            } else {
                // Return a result that indicates authentication is needed
                Result.failure(Exception("Authentication required"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun setupCalendarService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(CalendarScopes.CALENDAR)
        )
        credential.selectedAccount = account.account
        
        calendarService = Calendar.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName(context.getString(R.string.app_name))
            .build()
    }
    
    suspend fun fetchEvents(
        startTime: Long,
        endTime: Long,
        maxResults: Int = 50
    ): Result<List<CalendarEventData>> {
        return withContext(Dispatchers.IO) {
            try {
                val service = calendarService ?: return@withContext Result.failure(
                    Exception("Calendar service not initialized")
                )
                
                val startDateTime = com.google.api.client.util.DateTime(startTime)
                val endDateTime = com.google.api.client.util.DateTime(endTime)
                
                val events = service.events().list("primary")
                    .setTimeMin(startDateTime)
                    .setTimeMax(endDateTime)
                    .setMaxResults(maxResults)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute()
                
                val eventList = events.items?.map { event ->
                    CalendarEventData(
                        id = event.id ?: UUID.randomUUID().toString(),
                        googleEventId = event.id,
                        title = event.summary ?: "No Title",
                        description = event.description,
                        startTime = event.start?.dateTime?.value ?: event.start?.date?.value ?: startTime,
                        endTime = event.end?.dateTime?.value ?: event.end?.date?.value ?: endTime,
                        location = event.location,
                        isAllDay = event.start?.date != null,
                        attendees = event.attendees?.map { it.email } ?: emptyList(),
                        reminderMinutes = event.reminders?.overrides?.map { it.minutes } ?: emptyList(),
                        recurrenceRule = event.recurrence?.firstOrNull(),
                        lastSyncedAt = System.currentTimeMillis()
                    )
                } ?: emptyList()
                
                Result.success(eventList)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun createEvent(eventData: CalendarEventData): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val service = calendarService ?: return@withContext Result.failure(
                    Exception("Calendar service not initialized")
                )
                
                val event = Event().apply {
                    summary = eventData.title
                    description = eventData.description
                    location = eventData.location
                    
                    if (eventData.isAllDay) {
                        start = EventDateTime().setDate(
                            com.google.api.client.util.DateTime(
                                true, eventData.startTime, null
                            )
                        )
                        end = EventDateTime().setDate(
                            com.google.api.client.util.DateTime(
                                true, eventData.endTime, null
                            )
                        )
                    } else {
                        start = EventDateTime().setDateTime(
                            com.google.api.client.util.DateTime(eventData.startTime)
                        )
                        end = EventDateTime().setDateTime(
                            com.google.api.client.util.DateTime(eventData.endTime)
                        )
                    }
                    
                    // Add reminders
                    if (eventData.reminderMinutes.isNotEmpty()) {
                        reminders = Event.Reminders().apply {
                            useDefault = false
                            overrides = eventData.reminderMinutes.map { minutes ->
                                EventReminder().apply {
                                    method = "popup"
                                    this.minutes = minutes
                                }
                            }
                        }
                    }
                    
                    // Add recurrence if specified
                    eventData.recurrenceRule?.let { rrule ->
                        recurrence = listOf(rrule)
                    }
                }
                
                val createdEvent = service.events().insert("primary", event).execute()
                Result.success(createdEvent.id)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun updateEvent(eventData: CalendarEventData): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val service = calendarService ?: return@withContext Result.failure(
                    Exception("Calendar service not initialized")
                )
                
                val googleEventId = eventData.googleEventId ?: return@withContext Result.failure(
                    Exception("Google event ID required for updates")
                )
                
                // Get existing event
                val existingEvent = service.events().get("primary", googleEventId).execute()
                
                // Update fields
                existingEvent.apply {
                    summary = eventData.title
                    description = eventData.description
                    location = eventData.location
                    
                    if (eventData.isAllDay) {
                        start = EventDateTime().setDate(
                            com.google.api.client.util.DateTime(
                                true, eventData.startTime, null
                            )
                        )
                        end = EventDateTime().setDate(
                            com.google.api.client.util.DateTime(
                                true, eventData.endTime, null
                            )
                        )
                    } else {
                        start = EventDateTime().setDateTime(
                            com.google.api.client.util.DateTime(eventData.startTime)
                        )
                        end = EventDateTime().setDateTime(
                            com.google.api.client.util.DateTime(eventData.endTime)
                        )
                    }
                }
                
                service.events().update("primary", googleEventId, existingEvent).execute()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun deleteEvent(googleEventId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val service = calendarService ?: return@withContext Result.failure(
                    Exception("Calendar service not initialized")
                )
                
                service.events().delete("primary", googleEventId).execute()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    fun signOut(): Result<Unit> {
        return try {
            googleSignInClient?.signOut()
            calendarService = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun isAuthenticated(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null && GoogleSignIn.hasPermissions(account, Scope(CalendarScopes.CALENDAR))
    }
}

data class CalendarEventData(
    val id: String,
    val googleEventId: String? = null,
    val title: String,
    val description: String? = null,
    val startTime: Long,
    val endTime: Long,
    val location: String? = null,
    val isAllDay: Boolean = false,
    val attendees: List<String> = emptyList(),
    val reminderMinutes: List<Int> = emptyList(),
    val recurrenceRule: String? = null,
    val lastSyncedAt: Long = System.currentTimeMillis()
)