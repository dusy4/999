package com.ctonew.composemodular.data.network.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserRemoteDto(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: Long,
    val avatarUrl: String? = null,
    val username: String? = null,
    val bio: String? = null,
    val isFollowing: Boolean? = null,
)
