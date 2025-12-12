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
)
