package com.ctonew.composemodular.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ctonew.composemodular.domain.message.AttachmentRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AttachmentCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val attachmentRepository: AttachmentRepository,
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val retentionDays = inputData.getInt(KEY_RETENTION_DAYS, DEFAULT_RETENTION_DAYS)
            
            val cleanupTimestamp = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L)
            attachmentRepository.deleteAttachmentsOlderThan(cleanupTimestamp)
            
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val KEY_RETENTION_DAYS = "retention_days"
        const val DEFAULT_RETENTION_DAYS = 30
        private const val MAX_RETRIES = 2
    }
}
