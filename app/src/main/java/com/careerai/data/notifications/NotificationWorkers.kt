package com.careerai.data.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class HabitReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationService: SmartNotificationService
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val userId = inputData.getString("userId") ?: return Result.failure()
            notificationService.sendHabitReminder(userId)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

@HiltWorker
class GoalProgressWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationService: SmartNotificationService
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val userId = inputData.getString("userId") ?: return Result.failure()
            notificationService.sendGoalProgressUpdate(userId)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

@HiltWorker
class DailyInsightWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationService: SmartNotificationService
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val userId = inputData.getString("userId") ?: return Result.failure()
            notificationService.sendDailyInsight(userId)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

@HiltWorker
class WeeklySummaryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationService: SmartNotificationService
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val userId = inputData.getString("userId") ?: return Result.failure()
            notificationService.sendWeeklySummary(userId)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}