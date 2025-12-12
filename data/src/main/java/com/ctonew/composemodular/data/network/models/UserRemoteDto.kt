package com.ctonew.composemodular.data.network.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserRemoteDto(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: Long,
)
