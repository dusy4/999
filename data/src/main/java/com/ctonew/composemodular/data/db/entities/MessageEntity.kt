package com.ctonew.composemodular.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ThreadEntity::class,
            parentColumns = ["id"],
            childColumns = ["threadId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("threadId"),
        Index("userId"),
        Index("createdAt"),
    ],
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val threadId: String,
    val userId: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
)
