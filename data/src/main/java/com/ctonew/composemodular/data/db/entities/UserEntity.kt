package com.ctonew.composemodular.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val createdAt: Long,
    val avatarUrl: String = "",
    val username: String = "",
    val bio: String = "",
    val isFollowing: Boolean = false,
)
