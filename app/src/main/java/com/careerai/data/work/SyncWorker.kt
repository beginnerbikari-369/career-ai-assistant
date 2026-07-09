package com.careerai.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import com.careerai.data.auth.AuthService
import com.careerai.data.sync.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository,
    private val authService: AuthService
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        const val WORK_NAME = "sync_work"
        
        fun schedulePeriodicSync(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            
            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                15, TimeUnit.MINUTES // Sync every 15 minutes
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10, TimeUnit.SECONDS
                )
                .build()
            
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
        
        fun cancelPeriodicSync(workManager: WorkManager) {
            workManager.cancelUniqueWork(WORK_NAME)
        }
    }
    
    override suspend fun doWork(): ListenableWorker.Result {
        return try {
            // Check if user is authenticated
            if (!authService.isUserSignedIn) {
                return ListenableWorker.Result.success() // Skip sync if not signed in
            }
            
            // Perform sync
            val syncResult = syncRepository.syncAllUserData()
            
            syncResult.fold(
                onSuccess = { result ->
                    when (result) {
                        is com.careerai.data.sync.SyncResult.Success -> ListenableWorker.Result.success()
                        is com.careerai.data.sync.SyncResult.PartialFailure -> {
                            // Log partial failure but consider it success
                            // In production, you might want to retry failed tables
                            ListenableWorker.Result.success()
                        }
                        is com.careerai.data.sync.SyncResult.Failure -> ListenableWorker.Result.retry()
                    }
                },
                onFailure = { error ->
                    // Retry on failure with exponential backoff
                    ListenableWorker.Result.retry()
                }
            )
        } catch (e: Exception) {
            // Retry on exception
            ListenableWorker.Result.retry()
        }
    }
}