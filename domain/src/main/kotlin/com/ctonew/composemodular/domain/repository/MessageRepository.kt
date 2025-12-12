package com.ctonew.composemodular.domain.repository

import com.ctonew.composemodular.domain.models.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun observeMessages(threadId: String): Flow<List<Message>>
    suspend fun getMessages(threadId: String): List<Message>
    suspend fun getMessage(messageId: String): Message?
    suspend fun upsertMessage(message: Message)
    suspend fun deleteMessage(messageId: String)
    suspend fun syncMessages(threadId: String)
}
