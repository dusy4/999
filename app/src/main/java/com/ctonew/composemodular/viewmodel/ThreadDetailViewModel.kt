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

@HiltViewModel
class ThreadDetailViewModel @Inject constructor(
    private val threadRepository: ThreadRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    val users: StateFlow<Map<String, User>> = userRepository.observeAllUsers()
        .map { userList -> userList.associateBy { it.id } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    private val allThreads = threadRepository.observeThreads()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getThreadDetail(threadId: String): Flow<Thread?> {
        return allThreads.map { threads ->
            threads.find { it.id == threadId }
        }
    }

    fun getReplies(threadId: String): Flow<List<Thread>> {
        return allThreads.map { threads ->
            threads.filter { it.parentThreadId == threadId }
                .sortedByDescending { it.createdAt }
        }
    }

    fun toggleLike(threadId: String) {
        viewModelScope.launch {
            val thread = allThreads.value.find { it.id == threadId } ?: return@launch
            val updated = thread.copy(
                isLiked = !thread.isLiked,
                likeCount = if (thread.isLiked) thread.likeCount - 1 else thread.likeCount + 1
            )
            // In a real app, this would persist to the repository
        }
    }

    fun toggleRepost(threadId: String) {
        viewModelScope.launch {
            val thread = allThreads.value.find { it.id == threadId } ?: return@launch
            val updated = thread.copy(
                isReposted = !thread.isReposted,
                repostCount = if (thread.isReposted) thread.repostCount - 1 else thread.repostCount + 1
            )
            // In a real app, this would persist to the repository
        }
    }
}
