package com.ctonew.composemodular.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ctonew.composemodular.domain.message.MessageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SendQueuedMessagesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val messageRepository: MessageRepository,
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val queuedMessages = messageRepository.getOutboundQueue().first()
            
            var sentCount = 0
            for (message in queuedMessages) {
                try {
                    sendMessage(message.id)
                    messageRepository.removeFromOutboundQueue(message.id)
                    sentCount++
                } catch (e: Exception) {
                    // Log and continue with next message
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun sendMessage(messageId: String) {
        // In production, this would send the message to a remote API
        // Example: api.sendMessage(messageId)
    }

    companion object {
        private const val MAX_RETRIES = 3
    }
}
