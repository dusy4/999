package com.ctonew.composemodular.data.repository

import com.ctonew.composemodular.data.db.daos.UserDao
import com.ctonew.composemodular.data.mappers.toDomain
import com.ctonew.composemodular.data.mappers.toEntity
import com.ctonew.composemodular.data.network.api.UserApi
import com.ctonew.composemodular.domain.models.User
import com.ctonew.composemodular.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userApi: UserApi,
) : UserRepository {

    override fun observeUser(userId: String): Flow<User?> =
        userDao.observeUser(userId).map { it?.toDomain() }

    override fun observeAllUsers(): Flow<List<User>> =
        userDao.observeAllUsers().map { users -> users.map { it.toDomain() } }

    override suspend fun getUser(userId: String): User? =
        userDao.getUser(userId)?.toDomain()

    override suspend fun upsertUser(user: User) {
        userDao.upsertUser(user.toEntity())
    }

    override suspend fun syncUsers() {
        try {
            val remoteUsers = userApi.listUsers()
            val localUsers = remoteUsers.map { it.toEntity() }
            userDao.upsertUsers(localUsers)
        } catch (e: Exception) {
            // Log error but don't fail - use cached data
        }
    }
}
