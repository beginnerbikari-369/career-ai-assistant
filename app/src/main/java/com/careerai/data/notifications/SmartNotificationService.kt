package com.careerai.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.careerai.R
import com.careerai.data.analysis.PersonalizedAnalysisService
import com.careerai.data.repository.GoalRepository
import com.careerai.data.repository.HabitRepository
import com.careerai.domain.model.Goal
import com.careerai.domain.model.Habit
import com.careerai.domain.model.Priority
import com.careerai.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartNotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val goalRepository: GoalRepository,
    private val habitRepository: HabitRepository,
    private val personalizedAnalysisService: PersonalizedAnalysisService
) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        const val CHANNEL_ID_HABITS = "habits_reminders"
        const val CHANNEL_ID_GOALS = "goals_progress"
        const val CHANNEL_ID_INSIGHTS = "personalized_insights"
        const val CHANNEL_ID_MOTIVATION = "motivation"
        
        const val NOTIFICATION_ID_HABIT_REMINDER = 1001
        const val NOTIFICATION_ID_GOAL_MILESTONE = 1002
        const val NOTIFICATION_ID_DAILY_INSIGHT = 1003
        const val NOTIFICATION_ID_WEEKLY_SUMMARY = 1004
    }
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_HABITS,
                    "Habit Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Reminders for your daily habits"
                },
                NotificationChannel(
                    CHANNEL_ID_GOALS,
                    "Goal Progress",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Updates on your goal progress"
                },
                NotificationChannel(
                    CHANNEL_ID_INSIGHTS,
                    "AI Insights",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Personalized insights and recommendations"
                },
                NotificationChannel(
                    CHANNEL_ID_MOTIVATION,
                    "Motivation",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Motivational messages and celebrations"
                }
            )
            
            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
    
    fun scheduleSmartNotifications(userId: String) {
        // Schedule different types of notifications
        scheduleDailyHabitReminders(userId)
        scheduleGoalProgressChecks(userId)
        scheduleDailyInsights(userId)
        scheduleWeeklySummary(userId)
    }
    
    private fun scheduleDailyHabitReminders(userId: String) {
        val workRequest = PeriodicWorkRequestBuilder<HabitReminderWorker>(24, TimeUnit.HOURS)
            .setInputData(workDataOf("userId" to userId))
            .setInitialDelay(calculateHoursUntil(9, 0), TimeUnit.HOURS) // 9 AM
            .addTag("habit_reminders_$userId")
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "habit_reminders_$userId",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }
    
    private fun scheduleGoalProgressChecks(userId: String) {
        val workRequest = PeriodicWorkRequestBuilder<GoalProgressWorker>(7, TimeUnit.DAYS)
            .setInputData(workDataOf("userId" to userId))
            .setInitialDelay(calculateHoursUntil(18, 0), TimeUnit.HOURS) // 6 PM Sunday
            .addTag("goal_progress_$userId")
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "goal_progress_$userId",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }
    
    private fun scheduleDailyInsights(userId: String) {
        val workRequest = PeriodicWorkRequestBuilder<DailyInsightWorker>(24, TimeUnit.HOURS)
            .setInputData(workDataOf("userId" to userId))
            .setInitialDelay(calculateHoursUntil(20, 0), TimeUnit.HOURS) // 8 PM
            .addTag("daily_insights_$userId")
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "daily_insights_$userId",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }
    
    private fun scheduleWeeklySummary(userId: String) {
        val workRequest = PeriodicWorkRequestBuilder<WeeklySummaryWorker>(7, TimeUnit.DAYS)
            .setInputData(workDataOf("userId" to userId))
            .setInitialDelay(calculateHoursUntilWeekday(Calendar.SUNDAY, 19, 0), TimeUnit.HOURS) // Sunday 7 PM
            .addTag("weekly_summary_$userId")
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "weekly_summary_$userId",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }
    
    suspend fun sendHabitReminder(userId: String) {
        try {
            val habits = habitRepository.getActiveHabitsFlow(userId).first()
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val completions = habitRepository.getTodayCompletionsFlow(userId, System.currentTimeMillis()).first()
            
            val pendingHabits = habits.filter { habit ->
                completions.none { it.habitId == habit.id }
            }
            
            if (pendingHabits.isNotEmpty()) {
                val priorityHabit = pendingHabits.maxByOrNull { 
                    when (it.difficulty) {
                        com.careerai.domain.model.HabitDifficulty.EASY -> 1
                        com.careerai.domain.model.HabitDifficulty.MEDIUM -> 2
                        com.careerai.domain.model.HabitDifficulty.HARD -> 3
                    }
                }
                
                priorityHabit?.let { habit ->
                    showNotification(
                        channelId = CHANNEL_ID_HABITS,
                        notificationId = NOTIFICATION_ID_HABIT_REMINDER,
                        title = "⭐ Time for ${habit.name}!",
                        message = "Keep your ${habit.streakCount}-day streak going! ${getMotivationalMessage(habit)}",
                        actionText = "Mark Complete"
                    )
                }
            }
        } catch (e: Exception) {
            // Log error
        }
    }
    
    suspend fun sendGoalProgressUpdate(userId: String) {
        try {
            val goals = goalRepository.getIncompleteGoalsFlow(userId).first()
            val stagnantGoals = goals.filter { goal ->
                goal.progress < 10 || 
                (System.currentTimeMillis() - goal.updatedAt) > 7 * 24 * 60 * 60 * 1000 // 7 days
            }
            
            if (stagnantGoals.isNotEmpty()) {
                val highPriorityGoal = stagnantGoals.filter { it.priority == Priority.HIGH }.firstOrNull()
                    ?: stagnantGoals.first()
                
                showNotification(
                    channelId = CHANNEL_ID_GOALS,
                    notificationId = NOTIFICATION_ID_GOAL_MILESTONE,
                    title = "🎯 Goal Check-in",
                    message = "Your goal '${highPriorityGoal.title}' needs attention. Small progress is still progress!",
                    actionText = "Update Progress"
                )
            }
        } catch (e: Exception) {
            // Log error
        }
    }
    
    suspend fun sendDailyInsight(userId: String) {
        try {
            val insights = personalizedAnalysisService.generatePersonalizedInsights(userId).getOrNull()
            val topRecommendation = insights?.recommendations?.firstOrNull()
            
            if (topRecommendation != null) {
                showNotification(
                    channelId = CHANNEL_ID_INSIGHTS,
                    notificationId = NOTIFICATION_ID_DAILY_INSIGHT,
                    title = "💡 ${topRecommendation.title}",
                    message = topRecommendation.description,
                    actionText = "Learn More"
                )
            } else {
                // Fallback motivational message
                showNotification(
                    channelId = CHANNEL_ID_MOTIVATION,
                    notificationId = NOTIFICATION_ID_DAILY_INSIGHT,
                    title = "💪 Keep Growing!",
                    message = "Every day is a chance to improve. What will you accomplish today?",
                    actionText = "View Dashboard"
                )
            }
        } catch (e: Exception) {
            // Log error
        }
    }
    
    suspend fun sendWeeklySummary(userId: String) {
        try {
            val insights = personalizedAnalysisService.generatePersonalizedInsights(userId).getOrNull()
            val goalStats = goalRepository.getGoalStatistics(userId).getOrNull()
            
            val completedGoals = goalStats?.completedGoals ?: 0
            val message = when {
                completedGoals > 0 -> "🎉 Amazing! You completed $completedGoals goals this week. ${insights?.motivationalMessage ?: "Keep up the fantastic work!"}"
                insights?.progressAnalysis?.isNotEmpty() == true -> "📊 ${insights.progressAnalysis}"
                else -> "📈 Another week of growth! Check your progress and plan for the week ahead."
            }
            
            showNotification(
                channelId = CHANNEL_ID_INSIGHTS,
                notificationId = NOTIFICATION_ID_WEEKLY_SUMMARY,
                title = "📱 Weekly Summary",
                message = message,
                actionText = "View Summary"
            )
        } catch (e: Exception) {
            // Log error
        }
    }
    
    private fun showNotification(
        channelId: String,
        notificationId: Int,
        title: String,
        message: String,
        actionText: String? = null
    ) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        
        actionText?.let { text ->
            builder.addAction(
                R.drawable.ic_check,
                text,
                pendingIntent
            )
        }
        
        notificationManager.notify(notificationId, builder.build())
    }
    
    private fun getMotivationalMessage(habit: Habit): String {
        return when {
            habit.streakCount == 0 -> "Start your journey today!"
            habit.streakCount < 7 -> "You're building momentum!"
            habit.streakCount < 21 -> "Great consistency! Keep it up!"
            habit.streakCount < 66 -> "You're forming a strong habit!"
            else -> "Incredible dedication! You're a habit master!"
        }
    }
    
    private fun calculateHoursUntil(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If target time has passed today, schedule for tomorrow
            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        return (target.timeInMillis - now.timeInMillis) / (60 * 60 * 1000)
    }
    
    private fun calculateHoursUntilWeekday(weekday: Int, hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, weekday)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If target time has passed this week, schedule for next week
            if (before(now)) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
        
        return (target.timeInMillis - now.timeInMillis) / (60 * 60 * 1000)
    }
    
    fun cancelNotifications(userId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag("habit_reminders_$userId")
        WorkManager.getInstance(context).cancelAllWorkByTag("goal_progress_$userId")
        WorkManager.getInstance(context).cancelAllWorkByTag("daily_insights_$userId")
        WorkManager.getInstance(context).cancelAllWorkByTag("weekly_summary_$userId")
    }
}