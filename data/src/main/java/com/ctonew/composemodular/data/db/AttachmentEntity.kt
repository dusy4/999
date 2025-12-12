package com.ctonew.composemodular.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("messageId"),
        Index("createdAt"),
    ],
)
data class AttachmentEntity(
    @PrimaryKey val id: String,
    val messageId: String,
    val url: String,
    val type: String,
    val size: Long,
    val createdAt: Long,
)
