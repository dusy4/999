package com.ctonew.composemodular.data.mappers

import com.ctonew.composemodular.data.db.entities.MessageEntity
import com.ctonew.composemodular.data.network.models.MessageRemoteDto
import com.ctonew.composemodular.domain.models.Message

fun MessageEntity.toDomain(): Message = Message(
    id = id,
    threadId = threadId,
    userId = userId,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Message.toEntity(): MessageEntity = MessageEntity(
    id = id,
    threadId = threadId,
    userId = userId,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun MessageRemoteDto.toEntity(): MessageEntity = MessageEntity(
    id = id,
    threadId = threadId,
    userId = userId,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun MessageRemoteDto.toDomain(): Message = Message(
    id = id,
    threadId = threadId,
    userId = userId,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
