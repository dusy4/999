package com.ctonew.composemodular.domain.message

data class Message(
    val id: String,
    val conversationId: String,
    val content: String,
    val senderId: String,
    val timestamp: Long,
    val isOutbound: Boolean = false,
)
