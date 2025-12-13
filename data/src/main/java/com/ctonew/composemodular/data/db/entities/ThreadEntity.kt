package com.ctonew.composemodular.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "threads",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("userId"),
        Index("createdAt"),
        Index("updatedAt"),
    ],
)
data class ThreadEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val userId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val content: String = "",
    val mediaUrls: List<String> = emptyList(),
    val replyCount: Int = 0,
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val repostCount: Int = 0,
    val isReposted: Boolean = false,
    val parentThreadId: String? = null,
)
