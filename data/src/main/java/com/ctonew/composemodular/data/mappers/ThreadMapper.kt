package com.ctonew.composemodular.data.mappers

import com.ctonew.composemodular.data.db.entities.ThreadEntity
import com.ctonew.composemodular.data.network.models.ThreadRemoteDto
import com.ctonew.composemodular.domain.models.Thread

fun ThreadEntity.toDomain(): Thread = Thread(
    id = id,
    title = title,
    description = description,
    userId = userId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    content = content,
    mediaUrls = mediaUrls,
    replyCount = replyCount,
    likeCount = likeCount,
    isLiked = isLiked,
    repostCount = repostCount,
    isReposted = isReposted,
    parentThreadId = parentThreadId,
)

fun Thread.toEntity(): ThreadEntity = ThreadEntity(
    id = id,
    title = title,
    description = description,
    userId = userId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    content = content,
    mediaUrls = mediaUrls,
    replyCount = replyCount,
    likeCount = likeCount,
    isLiked = isLiked,
    repostCount = repostCount,
    isReposted = isReposted,
    parentThreadId = parentThreadId,
)

fun ThreadRemoteDto.toEntity(): ThreadEntity = ThreadEntity(
    id = id,
    title = title,
    description = description,
    userId = userId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    content = content ?: "",
    mediaUrls = mediaUrls ?: emptyList(),
    replyCount = replyCount ?: 0,
    likeCount = likeCount ?: 0,
    isLiked = isLiked ?: false,
    repostCount = repostCount ?: 0,
    isReposted = isReposted ?: false,
    parentThreadId = parentThreadId,
)

fun ThreadRemoteDto.toDomain(): Thread = Thread(
    id = id,
    title = title,
    description = description,
    userId = userId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    content = content ?: "",
    mediaUrls = mediaUrls ?: emptyList(),
    replyCount = replyCount ?: 0,
    likeCount = likeCount ?: 0,
    isLiked = isLiked ?: false,
    repostCount = repostCount ?: 0,
    isReposted = isReposted ?: false,
    parentThreadId = parentThreadId,
)
