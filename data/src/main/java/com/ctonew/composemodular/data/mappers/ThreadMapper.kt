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
)

fun Thread.toEntity(): ThreadEntity = ThreadEntity(
    id = id,
    title = title,
    description = description,
    userId = userId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun ThreadRemoteDto.toEntity(): ThreadEntity = ThreadEntity(
    id = id,
    title = title,
    description = description,
    userId = userId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun ThreadRemoteDto.toDomain(): Thread = Thread(
    id = id,
    title = title,
    description = description,
    userId = userId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
