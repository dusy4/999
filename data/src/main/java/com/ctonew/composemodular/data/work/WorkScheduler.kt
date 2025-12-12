package com.ctonew.composemodular.data.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val workManager = WorkManager.getInstance(context)

    fun schedulePeriodicSyncJobs() {
        scheduleBackfillWorker()
        scheduleAttachmentCleanupWorker()
        scheduleSendQueuedMessagesWorker()
    }

    private fun scheduleBackfillWorker() {
        val backfillConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val backfillWorkRequest = PeriodicWorkRequestBuilder<BackfillMessagesWorker>(
            15, TimeUnit.MINUTES,
        )
            .setConstraints(backfillConstraints)
            .setBackoffPolicy(
                BackoffPolicy.EXPONENTIAL,
                1, TimeUnit.MINUTES,
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "backfill_messages",
            ExistingPeriodicWorkPolicy.KEEP,
            backfillWorkRequest,
        )
    }

    private fun scheduleAttachmentCleanupWorker() {
        val cleanupConstraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val cleanupWorkRequest = PeriodicWorkRequestBuilder<AttachmentCleanupWorker>(
            1, TimeUnit.DAYS,
        )
            .setConstraints(cleanupConstraints)
            .setBackoffPolicy(
                BackoffPolicy.LINEAR,
                1, TimeUnit.HOURS,
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "attachment_cleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupWorkRequest,
        )
    }

    private fun scheduleSendQueuedMessagesWorker() {
        val sendConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .build()

        val sendWorkRequest = PeriodicWorkRequestBuilder<SendQueuedMessagesWorker>(
            15, TimeUnit.MINUTES,
        )
            .setConstraints(sendConstraints)
            .setBackoffPolicy(
                BackoffPolicy.EXPONENTIAL,
                1, TimeUnit.MINUTES,
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "send_queued_messages",
            ExistingPeriodicWorkPolicy.KEEP,
            sendWorkRequest,
        )
    }

    fun cancelAllWork() {
        workManager.cancelAllWork()
    }
}
