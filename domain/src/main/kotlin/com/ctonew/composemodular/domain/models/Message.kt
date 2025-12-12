package com.ctonew.composemodular.domain.models

data class Message(
    val id: String,
    val threadId: String,
    val userId: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
)
