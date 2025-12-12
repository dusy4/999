package com.ctonew.composemodular.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ctonew.composemodular.domain.chat.ChatRepository
import com.ctonew.composemodular.domain.chat.models.ChatConversation
import com.ctonew.composemodular.domain.chat.models.ConversationListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationListViewModel @Inject constructor(
    private val chatRepository: ChatRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationListState())
    val uiState: StateFlow<ConversationListState> = _uiState.asStateFlow()

    private var _currentPage = 0
    private val _pageSize = 20

    init {
        loadConversations()
        observeConversationUpdates()
    }

    fun loadConversations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            chatRepository.getConversations(_currentPage, _pageSize).collect { newConversations ->
                val updatedConversations = if (_currentPage == 0) {
                    newConversations
                } else {
                    _uiState.value.conversations + newConversations
                }

                _uiState.value = _uiState.value.copy(
                    conversations = updatedConversations,
                    isLoading = false,
                    hasMore = newConversations.size == _pageSize
                )
            }
        }
    }

    fun refreshConversations() {
        _currentPage = 0
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)

            chatRepository.getConversations(_currentPage, _pageSize).collect { newConversations ->
                _uiState.value = _uiState.value.copy(
                    conversations = newConversations,
                    isRefreshing = false,
                    hasMore = newConversations.size == _pageSize
                )
            }
        }
    }

    fun loadMoreConversations() {
        val currentState = _uiState.value
        if (!currentState.isLoading && !currentState.isRefreshing && currentState.hasMore) {
            _currentPage++
            loadConversations()
        }
    }

    fun searchConversations(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            chatRepository.searchConversations(query).fold(
                onSuccess = { results ->
                    _uiState.value = _uiState.value.copy(
                        conversations = results,
                        isLoading = false,
                        hasMore = false // No pagination for search results
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Search failed"
                    )
                }
            )
        }
    }

    fun markConversationAsRead(conversationId: String) {
        viewModelScope.launch {
            chatRepository.markConversationAsRead(conversationId).fold(
                onSuccess = {
                    // Update UI state to reflect read status
                    val updatedConversations = _uiState.value.conversations.map { conversation ->
                        if (conversation.id == conversationId) {
                            conversation.copy(unreadCount = 0)
                        } else conversation
                    }
                    _uiState.value = _uiState.value.copy(conversations = updatedConversations)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to mark as read"
                    )
                }
            )
        }
    }

    private fun observeConversationUpdates() {
        viewModelScope.launch {
            chatRepository.getConversationUpdates().collect { updatedConversation ->
                // Update conversation in the list
                val updatedConversations = _uiState.value.conversations.map { conversation ->
                    if (conversation.id == updatedConversation.id) {
                        updatedConversation
                    } else conversation
                }

                _uiState.value = _uiState.value.copy(conversations = updatedConversations)
            }
        }
    }
}