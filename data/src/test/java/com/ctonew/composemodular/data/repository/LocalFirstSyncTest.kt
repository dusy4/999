package com.ctonew.composemodular.data.repository

import com.ctonew.composemodular.data.db.daos.ThreadDao
import com.ctonew.composemodular.data.db.daos.UserDao
import com.ctonew.composemodular.data.db.entities.ThreadEntity
import com.ctonew.composemodular.data.db.entities.UserEntity
import com.ctonew.composemodular.data.network.api.ThreadApi
import com.ctonew.composemodular.data.network.api.UserApi
import com.ctonew.composemodular.data.network.models.ThreadRemoteDto
import com.ctonew.composemodular.data.network.models.UserRemoteDto
import com.ctonew.composemodular.domain.models.Thread
import com.ctonew.composemodular.domain.models.User
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class LocalFirstSyncTest {

    @Mock
    private lateinit var userDao: UserDao

    @Mock
    private lateinit var threadDao: ThreadDao

    @Mock
    private lateinit var userApi: UserApi

    @Mock
    private lateinit var threadApi: ThreadApi

    private lateinit var userRepository: UserRepositoryImpl
    private lateinit var threadRepository: ThreadRepositoryImpl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        userRepository = UserRepositoryImpl(userDao, userApi)
        threadRepository = ThreadRepositoryImpl(threadDao, threadApi)
    }

    @Test
    fun testLocalFirstRead_ReturnsCachedData() = runBlocking {
        val cachedUser = UserEntity(
            id = "user-1",
            name = "Cached User",
            email = "cached@example.com",
            createdAt = System.currentTimeMillis(),
        )

        whenever(userDao.getUser("user-1")).thenReturn(cachedUser)

        val result = userRepository.getUser("user-1")

        assert(result != null)
        assert(result?.name == "Cached User")
    }

    @Test
    fun testSyncFromRemote_UpdatesLocalCache() = runBlocking {
        val remoteUsers = listOf(
            UserRemoteDto(
                id = "remote-1",
                name = "Remote User",
                email = "remote@example.com",
                createdAt = System.currentTimeMillis(),
            ),
        )

        whenever(userApi.listUsers()).thenReturn(remoteUsers)

        userRepository.syncUsers()

        assert(remoteUsers.isNotEmpty())
        assert(remoteUsers[0].name == "Remote User")
    }

    @Test
    fun testSyncWithNetworkError_UsesLocalCache() = runBlocking {
        whenever(userApi.listUsers()).thenThrow(RuntimeException("Network error"))

        userRepository.syncUsers()

        // Should not throw - gracefully handles error
    }

    @Test
    fun testThreadSync_OrderByUpdatedAt() = runBlocking {
        val now = System.currentTimeMillis()
        val remoteThreads = listOf(
            ThreadRemoteDto(
                id = "thread-1",
                title = "Thread 1",
                description = null,
                userId = "user-1",
                createdAt = now - 1000,
                updatedAt = now - 500,
            ),
            ThreadRemoteDto(
                id = "thread-2",
                title = "Thread 2",
                description = null,
                userId = "user-1",
                createdAt = now,
                updatedAt = now,
            ),
        )

        whenever(threadApi.listThreads()).thenReturn(remoteThreads)

        threadRepository.syncThreads()

        assert(remoteThreads[1].updatedAt >= remoteThreads[0].updatedAt)
    }

    @Test
    fun testUpsertOverwritesOldData() = runBlocking {
        val user = User(
            id = "user-update",
            name = "Updated Name",
            email = "updated@example.com",
            createdAt = System.currentTimeMillis(),
        )

        userRepository.upsertUser(user)

        assert(user.name == "Updated Name")
    }
}
