package com.ctonew.composemodular.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    indices = [
        Index("conversationId"),
        Index("timestamp"),
    ],
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val content: String,
    val senderId: String,
    val timestamp: Long,
    val isOutbound: Boolean = false,
)

@Entity(
    tableName = "outbound_queue",
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class OutboundQueueEntity(
    @PrimaryKey val messageId: String,
    val addedAt: Long,
    val retryCount: Int = 0,
    val lastRetryTime: Long = 0,
)
