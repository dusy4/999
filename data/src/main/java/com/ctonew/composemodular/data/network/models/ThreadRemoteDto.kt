package com.ctonew.composemodular.data.network.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ThreadRemoteDto(
    val id: String,
    val title: String,
    val description: String?,
    val userId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val content: String? = null,
    val mediaUrls: List<String>? = null,
    val replyCount: Int? = null,
    val likeCount: Int? = null,
    val isLiked: Boolean? = null,
    val repostCount: Int? = null,
    val isReposted: Boolean? = null,
    val parentThreadId: String? = null,
)
