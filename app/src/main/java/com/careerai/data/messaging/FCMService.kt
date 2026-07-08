package com.careerai.data.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.careerai.R
import com.careerai.presentation.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {
    
    @Inject
    lateinit var notificationManager: NotificationManager
    
    companion object {
        const val CHANNEL_ID_REMINDERS = "reminders"
        const val CHANNEL_ID_HABITS = "habits"
        const val CHANNEL_ID_AI_SUGGESTIONS = "ai_suggestions"
        const val CHANNEL_ID_GOALS = "goals"
        const val CHANNEL_ID_CALENDAR = "calendar"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Handle FCM messages here
        remoteMessage.notification?.let { notification ->
            val title = notification.title ?: "Career AI Assistant"
            val body = notification.body ?: ""
            val channelId = remoteMessage.data["channel_id"] ?: CHANNEL_ID_REMINDERS
            
            showNotification(title, body, channelId)
        }
        
        // Handle data payload
        if (remoteMessage.data.isNotEmpty()) {
            handleDataPayload(remoteMessage.data)
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to your server
        sendTokenToServer(token)
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_REMINDERS,
                    "Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "General reminders and notifications"
                },
                NotificationChannel(
                    CHANNEL_ID_HABITS,
                    "Habits",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Habit tracking reminders"
                },
                NotificationChannel(
                    CHANNEL_ID_AI_SUGGESTIONS,
                    "AI Suggestions",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "AI-generated suggestions and insights"
                },
                NotificationChannel(
                    CHANNEL_ID_GOALS,
                    "Goals",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Goal milestones and progress updates"
                },
                NotificationChannel(
                    CHANNEL_ID_CALENDAR,
                    "Calendar",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Calendar events and meeting reminders"
                }
            )
            
            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
    
    private fun showNotification(title: String, body: String, channelId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification) // You'll need to add this icon
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
    
    private fun handleDataPayload(data: Map<String, String>) {
        // Handle different types of data payloads
        when (data["type"]) {
            "habit_reminder" -> {
                // Handle habit reminder
                val habitId = data["habit_id"]
                val habitName = data["habit_name"] ?: "Complete your habit"
                showNotification("Habit Reminder", habitName, CHANNEL_ID_HABITS)
            }
            "goal_milestone" -> {
                // Handle goal milestone
                val goalId = data["goal_id"]
                val goalTitle = data["goal_title"] ?: "Goal milestone reached"
                showNotification("Goal Achievement", goalTitle, CHANNEL_ID_GOALS)
            }
            "ai_suggestion" -> {
                // Handle AI suggestion
                val suggestion = data["suggestion"] ?: "New AI suggestion available"
                showNotification("AI Suggestion", suggestion, CHANNEL_ID_AI_SUGGESTIONS)
            }
            "calendar_reminder" -> {
                // Handle calendar reminder
                val eventTitle = data["event_title"] ?: "Upcoming event"
                val timeUntil = data["time_until"] ?: "soon"
                showNotification("Calendar Reminder", "$eventTitle starts $timeUntil", CHANNEL_ID_CALENDAR)
            }
        }
    }
    
    private fun sendTokenToServer(token: String) {
        // TODO: Send the FCM token to your server for storing
        // This will be implemented when we add the backend integration
    }
}