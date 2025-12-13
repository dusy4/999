package com.ctonew.composemodular.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ctonew.composemodular.domain.message.MessageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BackfillMessagesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val messageRepository: MessageRepository,
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val conversationId = inputData.getString(KEY_CONVERSATION_ID)
                ?: return Result.retry()
            val sinceTimestamp = inputData.getLong(KEY_SINCE_TIMESTAMP, 0L)
            
            backfillMessages(conversationId, sinceTimestamp)
            
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun backfillMessages(conversationId: String, sinceTimestamp: Long) {
        // In production, this would fetch from a remote API
        // Example: val messages = api.getMessages(conversationId, since = sinceTimestamp)
        // Then insert them into the database
        // messageRepository.insertMessage(...) for each message
    }

    companion object {
        const val KEY_CONVERSATION_ID = "conversation_id"
        const val KEY_SINCE_TIMESTAMP = "since_timestamp"
        private const val MAX_RETRIES = 3
    }
}
