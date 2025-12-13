package com.ctonew.composemodular.domain.models

data class Thread(
    val id: String,
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
