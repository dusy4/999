package com.ctonew.composemodular.domain.models

data class Thread(
    val id: String,
    val title: String,
    val description: String?,
    val userId: String,
    val createdAt: Long,
    val updatedAt: Long,
)
