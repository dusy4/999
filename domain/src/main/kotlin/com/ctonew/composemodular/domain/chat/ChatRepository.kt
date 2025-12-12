package com.ctonew.composemodular.domain.chat

import com.ctonew.composemodular.domain.chat.models.ChatConversation
import com.ctonew.composemodular.domain.chat.models.ChatMessage
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for chat functionality
 */
interface ChatRepository {
    
    /**
     * Get paginated list of conversations
     */
    fun getConversations(page: Int, pageSize: Int): Flow<List<ChatConversation>>
    
    /**
     * Get messages for a specific conversation
     */
    fun getMessages(conversationId: String, page: Int, pageSize: Int): Flow<List<ChatMessage>>
    
    /**
     * Send a new message
     */
    suspend fun sendMessage(message: ChatMessage): Result<Unit>
    
    /**
     * Mark conversation as read
     */
    suspend fun markConversationAsRead(conversationId: String): Result<Unit>
    
    /**
     * Get conversation by ID
     */
    suspend fun getConversationById(conversationId: String): Result<ChatConversation?>
    
    /**
     * Search conversations
     */
    suspend fun searchConversations(query: String): Result<List<ChatConversation>>
    
    /**
     * Stream conversation updates
     */
    fun getConversationUpdates(): Flow<ChatConversation>
    
    /**
     * Stream message updates for a conversation
     */
    fun getMessageUpdates(conversationId: String): Flow<ChatMessage>
}