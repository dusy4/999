package com.ctonew.composemodular.domain.message

data class Attachment(
    val id: String,
    val messageId: String,
    val url: String,
    val type: String,
    val size: Long,
    val createdAt: Long,
)
