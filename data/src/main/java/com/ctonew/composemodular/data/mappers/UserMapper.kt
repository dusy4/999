package com.ctonew.composemodular.data.mappers

import com.ctonew.composemodular.data.db.entities.UserEntity
import com.ctonew.composemodular.data.network.models.UserRemoteDto
import com.ctonew.composemodular.domain.models.User

fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    createdAt = createdAt,
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
    email = email,
    createdAt = createdAt,
)

fun UserRemoteDto.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
    email = email,
    createdAt = createdAt,
)

fun UserRemoteDto.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    createdAt = createdAt,
)
