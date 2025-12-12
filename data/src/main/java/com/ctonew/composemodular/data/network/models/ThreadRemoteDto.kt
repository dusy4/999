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
)
