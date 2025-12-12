package com.ctonew.composemodular.domain.chat.models

import java.time.LocalDateTime

/**
 * Represents a chat message with Telegram-style features
 */
data class ChatMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val timestamp: LocalDateTime,
    val messageType: MessageType = MessageType.TEXT,
    val status: MessageStatus = MessageStatus.SENT,
    val attachments: List<Attachment> = emptyList(),
    val replyToId: String? = null,
    val isFromMe: Boolean = false,
    val isEdited: Boolean = false
)

/**
 * Different types of messages supported
 */
sealed class MessageType {
    data object Text : MessageType()
    data class Image(val imageUrl: String) : MessageType()
    data class File(val fileName: String, val fileSize: Long, val fileUrl: String) : MessageType()
    data class Audio(val audioUrl: String, val duration: Long) : MessageType()
    data class Video(val videoUrl: String, val duration: Long) : MessageType()
}

/**
 * Message delivery and read status
 */
sealed class MessageStatus {
    data object Sending : MessageStatus()
    data object Sent : MessageStatus()
    data object Delivered : MessageStatus()
    data object Read : MessageStatus()
    data class Failed(val error: String) : MessageStatus()
}

/**
 * Attachment types for messages
 */
sealed class Attachment {
    data class Image(
        val url: String,
        val width: Int,
        val height: Int,
        val fileSize: Long
    ) : Attachment()
    
    data class File(
        val name: String,
        val url: String,
        val fileSize: Long,
        val mimeType: String
    ) : Attachment()
    
    data class Audio(
        val url: String,
        val duration: Long,
        val fileSize: Long
    ) : Attachment()
    
    data class Video(
        val url: String,
        val duration: Long,
        val fileSize: Long,
        val width: Int,
        val height: Int
    ) : Attachment()
}