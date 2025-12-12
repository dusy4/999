package com.ctonew.composemodular.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.ctonew.composemodular.data.db.daos.MessageDao
import com.ctonew.composemodular.data.mappers.toDomain
import com.ctonew.composemodular.data.mappers.toEntity
import com.ctonew.composemodular.data.network.api.MessageApi
import com.ctonew.composemodular.domain.models.Message
import com.ctonew.composemodular.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val messageApi: MessageApi,
) : MessageRepository, PagingMessageRepository {

    override fun observeMessagesPaged(threadId: String): Flow<PagingData<Message>> =
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { messageDao.observeMessagesPaged(threadId) },
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }

    override fun observeMessages(threadId: String): Flow<List<Message>> =
        messageDao.observeMessages(threadId).map { messages -> messages.map { it.toDomain() } }

    override suspend fun getMessages(threadId: String): List<Message> =
        messageDao.getMessages(threadId).map { it.toDomain() }

    override suspend fun getMessage(messageId: String): Message? =
        messageDao.getMessage(messageId)?.toDomain()

    override suspend fun upsertMessage(message: Message) {
        messageDao.upsertMessage(message.toEntity())
    }

    override suspend fun deleteMessage(messageId: String) {
        messageDao.deleteMessage(messageId)
    }

    override suspend fun syncMessages(threadId: String) {
        try {
            val remoteMessages = messageApi.listMessages(threadId)
            val localMessages = remoteMessages.map { it.toEntity() }
            messageDao.upsertMessages(localMessages)
        } catch (e: Exception) {
            // Log error but don't fail - use cached data
        }
    }
}
