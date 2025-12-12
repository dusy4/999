package com.ctonew.composemodular.domain.chat.models

import java.time.LocalDateTime

/**
 * Represents a chat conversation/thread
 */
data class ChatConversation(
    val id: String,
    val title: String,
    val participantIds: List<String>,
    val lastMessage: ChatMessage?,
    val unreadCount: Int = 0,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val conversationType: ConversationType = ConversationType.PRIVATE,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val avatarUrl: String? = null
)

/**
 * Types of conversations
 */
sealed class ConversationType {
    data object Private : ConversationType()
    data object Group : ConversationType()
    data object Channel : ConversationType()
}

/**
 * UI state for conversation list
 */
data class ConversationListState(
    val conversations: List<ChatConversation> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true
)

/**
 * UI state for individual conversation
 */
data class ConversationState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true,
    val currentPage: Int = 0
)