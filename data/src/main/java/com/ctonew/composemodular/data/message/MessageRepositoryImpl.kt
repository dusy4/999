package com.ctonew.composemodular.data.message

import com.ctonew.composemodular.data.db.MessageDao
import com.ctonew.composemodular.data.db.OutboundQueueDao
import com.ctonew.composemodular.data.db.MessageEntity
import com.ctonew.composemodular.data.db.OutboundQueueEntity
import com.ctonew.composemodular.domain.message.Message
import com.ctonew.composemodular.domain.message.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val queueDao: OutboundQueueDao,
) : MessageRepository {
    
    override fun getAllMessages(conversationId: String): Flow<List<Message>> =
        messageDao.getMessages(conversationId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getMessage(id: String): Message? =
        messageDao.getMessageById(id)?.toDomain()

    override suspend fun insertMessage(message: Message) {
        messageDao.insertMessage(message.toEntity())
    }

    override suspend fun updateMessage(message: Message) {
        messageDao.updateMessage(message.toEntity())
    }

    override suspend fun deleteMessage(id: String) {
        messageDao.deleteMessageById(id)
    }

    override fun getOutboundQueue(): Flow<List<Message>> =
        queueDao.getQueuedMessages().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun addToOutboundQueue(message: Message) {
        messageDao.insertMessage(message.toEntity())
        queueDao.enqueue(
            OutboundQueueEntity(
                messageId = message.id,
                addedAt = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun removeFromOutboundQueue(messageId: String) {
        queueDao.dequeue(messageId)
    }

    private fun Message.toEntity() = MessageEntity(
        id = id,
        conversationId = conversationId,
        content = content,
        senderId = senderId,
        timestamp = timestamp,
        isOutbound = isOutbound,
    )

    private fun MessageEntity.toDomain() = Message(
        id = id,
        conversationId = conversationId,
        content = content,
        senderId = senderId,
        timestamp = timestamp,
        isOutbound = isOutbound,
    )
}
