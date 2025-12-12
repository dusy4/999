package com.ctonew.composemodular.data.network.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessageRemoteDto(
    val id: String,
    val threadId: String,
    val userId: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
)
