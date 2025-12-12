package com.ctonew.composemodular.domain.repository

import com.ctonew.composemodular.domain.models.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUser(userId: String): Flow<User?>
    fun observeAllUsers(): Flow<List<User>>
    suspend fun getUser(userId: String): User?
    suspend fun upsertUser(user: User)
    suspend fun syncUsers()
}
