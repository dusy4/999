package com.ctonew.composemodular.data.message

import com.ctonew.composemodular.data.db.AttachmentDao
import com.ctonew.composemodular.data.db.AttachmentEntity
import com.ctonew.composemodular.domain.message.Attachment
import com.ctonew.composemodular.domain.message.AttachmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AttachmentRepositoryImpl @Inject constructor(
    private val attachmentDao: AttachmentDao,
) : AttachmentRepository {
    
    override fun getAttachments(messageId: String): Flow<List<Attachment>> =
        attachmentDao.getAttachments(messageId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun insertAttachment(attachment: Attachment) {
        attachmentDao.insertAttachment(attachment.toEntity())
    }

    override suspend fun deleteAttachment(id: String) {
        attachmentDao.deleteAttachment(id)
    }

    override suspend fun deleteAttachmentsOlderThan(timestampMs: Long) {
        attachmentDao.deleteAttachmentsOlderThan(timestampMs)
    }

    private fun Attachment.toEntity() = AttachmentEntity(
        id = id,
        messageId = messageId,
        url = url,
        type = type,
        size = size,
        createdAt = createdAt,
    )

    private fun AttachmentEntity.toDomain() = Attachment(
        id = id,
        messageId = messageId,
        url = url,
        type = type,
        size = size,
        createdAt = createdAt,
    )
}
