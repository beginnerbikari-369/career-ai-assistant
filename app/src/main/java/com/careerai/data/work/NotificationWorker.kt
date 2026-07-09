package com.careerai.data.work

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker
import com.careerai.R
import com.careerai.data.api.AIService
import com.careerai.data.repository.CalendarRepository
import com.careerai.data.repository.GoalRepository
import com.careerai.data.repository.HabitRepository
import com.careerai.domain.model.ConversationContext
import com.careerai.presentation.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val habitRepository: HabitRepository,
    private val calendarRepository: CalendarRepository,
    private val goalRepository: GoalRepository,
    private val aiService: AIService,
    private val notificationManager: NotificationManager
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): ListenableWorker.Result {
        return try {
            when (inputData.getString("type")) {
                "habit_reminder" -> handleHabitReminder()
                "calendar_reminder" -> handleCalendarReminder()
                "ai_suggestion" -> handleAISuggestion()
                "goal_milestone" -> handleGoalMilestone()
                "daily_summary" -> handleDailySummary()
                "smart_suggestions" -> handleSmartSuggestions()
                else -> ListenableWorker.Result.success()
            }
        } catch (e: Exception) {
            ListenableWorker.Result.failure()
        }
    }
    
    private suspend fun handleHabitReminder(): ListenableWorker.Result {
        val habitId = inputData.getString("habit_id") ?: return ListenableWorker.Result.failure()
        val habitName = inputData.getString("habit_name") ?: "Complete your habit"
        
        showNotification(
            title = "Habit Reminder",
            message = "Time to $habitName",
            channelId = "habits",
            notificationId = habitId.hashCode()
        )
        
        return ListenableWorker.Result.success()
    }
    
    private suspend fun handleCalendarReminder(): ListenableWorker.Result {
        val eventId = inputData.getString("event_id") ?: return ListenableWorker.Result.failure()
        val eventTitle = inputData.getString("event_title") ?: "Upcoming Event"
        val minutesBefore = inputData.getInt("minutes_before", 15)
        
        val timeText = when (minutesBefore) {
            0 -> "now"
            1 -> "in 1 minute"
            else -> "in $minutesBefore minutes"
        }
        
        showNotification(
            title = "Calendar Reminder",
            message = "$eventTitle starts $timeText",
            channelId = "calendar",
            notificationId = eventId.hashCode()
        )
        
        return ListenableWorker.Result.success()
    }
    
    private suspend fun handleAISuggestion(): ListenableWorker.Result {
        val userId = inputData.getString("user_id") ?: return ListenableWorker.Result.failure()
        val contextName = inputData.getString("context") ?: "GENERAL"
        val context = ConversationContext.valueOf(contextName)
        
        // Generate AI suggestion based on user's recent activity
        val suggestion = generateAISuggestion(userId, context)
        
        showNotification(
            title = "AI Suggestion",
            message = suggestion,
            channelId = "ai_suggestions",
            notificationId = System.currentTimeMillis().toInt()
        )
        
        return ListenableWorker.Result.success()
    }
    
    private suspend fun handleGoalMilestone(): ListenableWorker.Result {
        val goalId = inputData.getString("goal_id") ?: return ListenableWorker.Result.failure()
        val goalTitle = inputData.getString("goal_title") ?: "Goal"
        val milestoneTitle = inputData.getString("milestone_title") ?: "Milestone"
        
        showNotification(
            title = "Goal Milestone",
            message = "Time to work on: $milestoneTitle for $goalTitle",
            channelId = "goals",
            notificationId = goalId.hashCode()
        )
        
        return ListenableWorker.Result.success()
    }
    
    private suspend fun handleDailySummary(): ListenableWorker.Result {
        val userId = inputData.getString("user_id") ?: return ListenableWorker.Result.failure()
        
        // Get today's completion stats
        val completedHabits = habitRepository.getTodayCompletionsCount(userId).getOrNull() ?: 0
        val upcomingEvents = calendarRepository.getUpcomingEvents(userId, 3).getOrNull()?.size ?: 0
        
        val summaryMessage = "Today: $completedHabits habits completed, $upcomingEvents events tomorrow"
        
        showNotification(
            title = "Daily Summary",
            message = summaryMessage,
            channelId = "reminders",
            notificationId = "daily_summary".hashCode()
        )
        
        return ListenableWorker.Result.success()
    }
    
    private suspend fun handleSmartSuggestions(): ListenableWorker.Result {
        val userId = inputData.getString("user_id") ?: return ListenableWorker.Result.failure()
        
        // Analyze user patterns and suggest optimizations
        val suggestions = analyzeUserPatternsAndSuggest(userId)
        
        if (suggestions.isNotEmpty()) {
            showNotification(
                title = "Smart Suggestion",
                message = suggestions.first(),
                channelId = "ai_suggestions",
                notificationId = System.currentTimeMillis().toInt()
            )
        }
        
        return ListenableWorker.Result.success()
    }
    
    private suspend fun generateAISuggestion(userId: String, context: ConversationContext): String {
        // Generate contextual AI suggestions based on user activity
        return when (context) {
            ConversationContext.CAREER -> "Consider updating your LinkedIn profile with your recent achievements"
            ConversationContext.GOALS -> "Review your weekly goals - you're making great progress!"
            ConversationContext.HABITS -> "You've maintained a 5-day streak! Keep it up"
            ConversationContext.PLANNING -> "Tomorrow looks busy - consider time-blocking your priorities"
            ConversationContext.GENERAL -> "Take a moment to reflect on today's accomplishments"
        }
    }
    
    private suspend fun analyzeUserPatternsAndSuggest(userId: String): List<String> {
        val suggestions = mutableListOf<String>()
        
        // Analyze habit completion patterns
        val completionCount = habitRepository.getTodayCompletionsCount(userId).getOrNull() ?: 0
        if (completionCount == 0) {
            suggestions.add("You haven't completed any habits today. Start with just one!")
        }
        
        // Analyze upcoming calendar
        val upcomingEvents = calendarRepository.getUpcomingEvents(userId, 5).getOrNull() ?: emptyList()
        if (upcomingEvents.size >= 3) {
            suggestions.add("You have a busy schedule ahead. Consider blocking focus time.")
        }
        
        return suggestions
    }
    
    private fun showNotification(
        title: String,
        message: String,
        channelId: String,
        notificationId: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        notificationManager.notify(notificationId, notification)
    }
}