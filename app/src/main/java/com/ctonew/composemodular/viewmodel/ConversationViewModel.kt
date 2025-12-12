package com.ctonew.composemodular.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ctonew.composemodular.domain.chat.ChatRepository
import com.ctonew.composemodular.domain.chat.models.ChatMessage
import com.ctonew.composemodular.domain.chat.models.ConversationState
import com.ctonew.composemodular.domain.chat.models.MessageStatus
import com.ctonew.composemodular.domain.chat.models.MessageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val conversationId: String = savedStateHandle["conversationId"] ?: ""

    private val _uiState = MutableStateFlow(ConversationState())
    val uiState: StateFlow<ConversationState> = _uiState.asStateFlow()

    private var _currentPage = 0
    private val _pageSize = 30

    init {
        if (conversationId.isNotEmpty()) {
            loadMessages()
            observeMessageUpdates()
        }
    }

    fun loadMessages() {
        if (conversationId.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            chatRepository.getMessages(conversationId, _currentPage, _pageSize).collect { newMessages ->
                val updatedMessages = if (_currentPage == 0) {
                    newMessages
                } else {
                    newMessages + _uiState.value.messages
                }

                _uiState.value = _uiState.value.copy(
                    messages = updatedMessages,
                    isLoading = false,
                    hasMore = newMessages.size == _pageSize
                )
            }
        }
    }

    fun loadMoreMessages() {
        val currentState = _uiState.value
        if (!currentState.isLoading && currentState.hasMore && _currentPage > 0) {
            _currentPage++
            loadMessages()
        } else if (currentState.hasMore && _currentPage == 0) {
            _currentPage++
            loadMessages()
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank() || conversationId.isEmpty()) return

        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = "user1", // TODO: Get from user session
            content = content.trim(),
            timestamp = LocalDateTime.now(),
            messageType = MessageType.Text,
            status = MessageStatus.Sending,
            isFromMe = true
        )

        // Add to UI immediately
        val currentMessages = _uiState.value.messages
        _uiState.value = _uiState.value.copy(
            messages = currentMessages + message,
            isSending = true
        )

        viewModelScope.launch {
            chatRepository.sendMessage(message).fold(
                onSuccess = {
                    // Update status to sent
                    val updatedMessages = _uiState.value.messages.map { msg ->
                        if (msg.id == message.id) {
                            msg.copy(status = MessageStatus.Sent)
                        } else msg
                    }
                    _uiState.value = _uiState.value.copy(
                        messages = updatedMessages,
                        isSending = false
                    )
                },
                onFailure = { error ->
                    // Update status to failed
                    val updatedMessages = _uiState.value.messages.map { msg ->
                        if (msg.id == message.id) {
                            msg.copy(status = MessageStatus.Failed(error.message ?: "Send failed"))
                        } else msg
                    }
                    _uiState.value = _uiState.value.copy(
                        messages = updatedMessages,
                        isSending = false,
                        error = error.message ?: "Failed to send message"
                    )
                }
            )
        }
    }

    fun retrySendMessage(messageId: String) {
        val message = _uiState.value.messages.find { it.id == messageId } ?: return
        if (message.status !is MessageStatus.Failed) return

        // Update status to sending
        val updatedMessages = _uiState.value.messages.map { msg ->
            if (msg.id == messageId) {
                msg.copy(status = MessageStatus.Sending)
            } else msg
        }
        _uiState.value = _uiState.value.copy(messages = updatedMessages)

        viewModelScope.launch {
            chatRepository.sendMessage(message).fold(
                onSuccess = {
                    // Update status to sent
                    val finalMessages = updatedMessages.map { msg ->
                        if (msg.id == messageId) {
                            msg.copy(status = MessageStatus.Sent)
                        } else msg
                    }
                    _uiState.value = _uiState.value.copy(messages = finalMessages)
                },
                onFailure = { error ->
                    // Update status to failed
                    val finalMessages = updatedMessages.map { msg ->
                        if (msg.id == messageId) {
                            msg.copy(status = MessageStatus.Failed(error.message ?: "Send failed"))
                        } else msg
                    }
                    _uiState.value = _uiState.value.copy(
                        messages = finalMessages,
                        error = error.message ?: "Failed to resend message"
                    )
                }
            )
        }
    }

    fun markAsRead() {
        if (conversationId.isEmpty()) return

        viewModelScope.launch {
            chatRepository.markConversationAsRead(conversationId)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun observeMessageUpdates() {
        if (conversationId.isEmpty()) return

        viewModelScope.launch {
            chatRepository.getMessageUpdates(conversationId).collect { newMessage ->
                val currentMessages = _uiState.value.messages
                _uiState.value = _uiState.value.copy(
                    messages = currentMessages + newMessage
                )
            }
        }
    }
}