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
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val threadRepository: ThreadRepository,
) : ViewModel() {

    val users: StateFlow<Map<String, User>> = userRepository.observeAllUsers()
        .map { userList -> userList.associateBy { it.id } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    private val allThreads = threadRepository.observeThreads()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getUser(userId: String): Flow<User?> {
        return userRepository.observeAllUsers()
            .map { users -> users.find { it.id == userId } }
    }

    fun getUserThreads(userId: String): Flow<List<Thread>> {
        return allThreads.map { threads ->
            threads.filter { it.userId == userId }
                .sortedByDescending { it.createdAt }
        }
    }

    fun toggleFollow(userId: String) {
        viewModelScope.launch {
            val user = users.value[userId] ?: return@launch
            val updated = user.copy(isFollowing = !user.isFollowing)
            // In a real app, this would persist to the repository
        }
    }
}
