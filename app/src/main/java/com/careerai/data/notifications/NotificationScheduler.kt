package com.careerai.data.notifications

import androidx.work.*
import com.careerai.data.work.NotificationWorker
import com.careerai.domain.model.ConversationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    private val workManager: WorkManager
) {
    
    companion object {
        private const val HABIT_REMINDER_TAG = "habit_reminder"
        private const val CALENDAR_REMINDER_TAG = "calendar_reminder"
        private const val AI_SUGGESTION_TAG = "ai_suggestion"
        private const val GOAL_MILESTONE_TAG = "goal_milestone"
        private const val DAILY_SUMMARY_TAG = "daily_summary"
    }
    
    fun scheduleHabitReminder(
        habitId: String,
        habitName: String,
        reminderTime: String // HH:mm format
    ) {
        val inputData = workDataOf(
            "type" to "habit_reminder",
            "habit_id" to habitId,
            "habit_name" to habitName,
            "reminder_time" to reminderTime
        )
        
        val delay = calculateDelayUntilTime(reminderTime)
        
        val request = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(inputData)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(HABIT_REMINDER_TAG)
            .addTag("habit_$habitId")
            .build()
        
        workManager.enqueueUniqueWork(
            "habit_reminder_$habitId",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
    
    fun scheduleCalendarReminder(
        eventId: String,
        eventTitle: String,
        eventStartTime: Long,
        minutesBefore: Int
    ) {
        val reminderTime = eventStartTime - (minutesBefore * 60 * 1000)
        val delay = reminderTime - System.currentTimeMillis()
        
        if (delay > 0) {
            val inputData = workDataOf(
                "type" to "calendar_reminder",
                "event_id" to eventId,
                "event_title" to eventTitle,
                "minutes_before" to minutesBefore
            )
            
            val request = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInputData(inputData)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(CALENDAR_REMINDER_TAG)
                .addTag("event_$eventId")
                .build()
            
            workManager.enqueueUniqueWork(
                "calendar_reminder_${eventId}_$minutesBefore",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
    
    fun scheduleAISuggestion(
        userId: String,
        context: ConversationContext,
        delayHours: Int = 24
    ) {
        val inputData = workDataOf(
            "type" to "ai_suggestion",
            "user_id" to userId,
            "context" to context.name,
            "suggestion_type" to "daily_insight"
        )
        
        val request = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(inputData)
            .setInitialDelay(delayHours.toLong(), TimeUnit.HOURS)
            .addTag(AI_SUGGESTION_TAG)
            .build()
        
        workManager.enqueueUniqueWork(
            "ai_suggestion_${context.name}_$userId",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
    
    fun scheduleGoalMilestoneNotification(
        goalId: String,
        goalTitle: String,
        milestoneTitle: String,
        targetDate: Long
    ) {
        val delay = targetDate - System.currentTimeMillis()
        
        if (delay > 0) {
            val inputData = workDataOf(
                "type" to "goal_milestone",
                "goal_id" to goalId,
                "goal_title" to goalTitle,
                "milestone_title" to milestoneTitle
            )
            
            val request = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInputData(inputData)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(GOAL_MILESTONE_TAG)
                .addTag("goal_$goalId")
                .build()
            
            workManager.enqueueUniqueWork(
                "goal_milestone_${goalId}_${targetDate}",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
    
    fun scheduleDailySummary(userId: String, timeOfDay: String = "20:00") {
        val inputData = workDataOf(
            "type" to "daily_summary",
            "user_id" to userId
        )
        
        val delay = calculateDelayUntilTime(timeOfDay)
        
        val request = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
            .setInputData(inputData)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(DAILY_SUMMARY_TAG)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            "daily_summary_$userId",
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }
    
    fun scheduleSmartSuggestions(userId: String) {
        // Schedule periodic AI-powered suggestions based on user behavior
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val inputData = workDataOf(
            "type" to "smart_suggestions",
            "user_id" to userId
        )
        
        val request = PeriodicWorkRequestBuilder<NotificationWorker>(6, TimeUnit.HOURS)
            .setInputData(inputData)
            .setConstraints(constraints)
            .addTag("smart_suggestions")
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            "smart_suggestions_$userId",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
    
    fun cancelHabitReminder(habitId: String) {
        workManager.cancelAllWorkByTag("habit_$habitId")
    }
    
    fun cancelCalendarReminder(eventId: String) {
        workManager.cancelAllWorkByTag("event_$eventId")
    }
    
    fun cancelGoalNotifications(goalId: String) {
        workManager.cancelAllWorkByTag("goal_$goalId")
    }
    
    fun cancelAllNotifications() {
        workManager.cancelAllWork()
    }
    
    private fun calculateDelayUntilTime(timeString: String): Long {
        // Parse time string (HH:mm) and calculate delay until next occurrence
        val timeParts = timeString.split(":")
        if (timeParts.size != 2) return 0
        
        val targetHour = timeParts[0].toIntOrNull() ?: return 0
        val targetMinute = timeParts[1].toIntOrNull() ?: return 0
        
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        
        // Set target time for today
        calendar.set(java.util.Calendar.HOUR_OF_DAY, targetHour)
        calendar.set(java.util.Calendar.MINUTE, targetMinute)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        
        var targetTime = calendar.timeInMillis
        
        // If target time has passed today, schedule for tomorrow
        if (targetTime <= now) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
            targetTime = calendar.timeInMillis
        }
        
        return targetTime - now
    }
}