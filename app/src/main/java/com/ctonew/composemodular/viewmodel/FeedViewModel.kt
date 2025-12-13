package com.ctonew.composemodular.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.ctonew.composemodular.domain.models.Thread
import com.ctonew.composemodular.domain.models.User
import com.ctonew.composemodular.domain.repository.ThreadRepository
import com.ctonew.composemodular.domain.repository.UserRepository
import javax.inject.Inject

data class FeedUiState(
    val threads: List<Thread> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
    val currentPage: Int = 0,
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val threadRepository: ThreadRepository,
    private val userRepository: UserRepository,
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState = _uiState.asStateFlow()

    val users: StateFlow<Map<String, User>> = userRepository.observeAllUsers()
        .map { userList -> userList.associateBy { it.id } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    init {
        loadFeed()
    }

    private fun loadFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                threadRepository.observeThreads()
                    .collect { threads ->
                        _uiState.update { state ->
                            state.copy(
                                threads = threads,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load threads"
                    )
                }
            }
        }
    }

    fun refreshFeed() {
        viewModelScope.launch {
            try {
                threadRepository.syncThreads()
                loadFeed()
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = e.message ?: "Failed to refresh threads"
                    )
                }
            }
        }
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            try {
                val currentPage = _uiState.value.currentPage
                threadRepository.syncThreads()
                _uiState.update { state ->
                    state.copy(
                        isLoadingMore = false,
                        currentPage = currentPage + 1,
                        hasMore = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoadingMore = false,
                        error = e.message ?: "Failed to load more threads"
                    )
                }
            }
        }
    }

    fun toggleLike(threadId: String) {
        val currentThread = _uiState.value.threads.find { it.id == threadId } ?: return
        val updatedThread = currentThread.copy(
            isLiked = !currentThread.isLiked,
            likeCount = if (currentThread.isLiked) currentThread.likeCount - 1 else currentThread.likeCount + 1
        )
        updateThread(updatedThread)
    }

    fun toggleRepost(threadId: String) {
        val currentThread = _uiState.value.threads.find { it.id == threadId } ?: return
        val updatedThread = currentThread.copy(
            isReposted = !currentThread.isReposted,
            repostCount = if (currentThread.isReposted) currentThread.repostCount - 1 else currentThread.repostCount + 1
        )
        updateThread(updatedThread)
    }

    private fun updateThread(thread: Thread) {
        _uiState.update { state ->
            state.copy(
                threads = state.threads.map { if (it.id == thread.id) thread else it }
            )
        }
    }
}
