package com.ctonew.composemodular.domain.message

import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getAllMessages(conversationId: String): Flow<List<Message>>
    suspend fun getMessage(id: String): Message?
    suspend fun insertMessage(message: Message)
    suspend fun updateMessage(message: Message)
    suspend fun deleteMessage(id: String)
    
    fun getOutboundQueue(): Flow<List<Message>>
    suspend fun addToOutboundQueue(message: Message)
    suspend fun removeFromOutboundQueue(messageId: String)
}
