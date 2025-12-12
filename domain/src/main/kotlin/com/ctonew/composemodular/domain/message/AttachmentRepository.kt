package com.ctonew.composemodular.domain.message

import kotlinx.coroutines.flow.Flow

interface AttachmentRepository {
    fun getAttachments(messageId: String): Flow<List<Attachment>>
    suspend fun insertAttachment(attachment: Attachment)
    suspend fun deleteAttachment(id: String)
    suspend fun deleteAttachmentsOlderThan(timestampMs: Long)
}
