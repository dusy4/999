package com.ctonew.composemodular.domain.models

data class User(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: Long,
    val avatarUrl: String = "",
    val username: String = "",
    val bio: String = "",
    val isFollowing: Boolean = false,
)
