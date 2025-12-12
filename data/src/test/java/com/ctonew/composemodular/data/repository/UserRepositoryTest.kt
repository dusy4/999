package com.ctonew.composemodular.data.repository

import com.ctonew.composemodular.data.db.daos.UserDao
import com.ctonew.composemodular.data.db.entities.UserEntity
import com.ctonew.composemodular.data.network.api.UserApi
import com.ctonew.composemodular.data.network.models.UserRemoteDto
import com.ctonew.composemodular.domain.models.User
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class UserRepositoryTest {

    @Mock
    private lateinit var userDao: UserDao

    @Mock
    private lateinit var userApi: UserApi

    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = UserRepositoryImpl(userDao, userApi)
    }

    @Test
    fun testUpsertUser() = runBlocking {
        val user = User(
            id = "1",
            name = "John Doe",
            email = "john@example.com",
            createdAt = System.currentTimeMillis(),
        )

        repository.upsertUser(user)

        assert(user.id == "1")
    }

    @Test
    fun testGetUser() = runBlocking {
        val userEntity = UserEntity(
            id = "1",
            name = "John Doe",
            email = "john@example.com",
            createdAt = System.currentTimeMillis(),
        )

        whenever(userDao.getUser("1")).thenReturn(userEntity)

        val result = repository.getUser("1")

        assert(result?.id == "1")
        assert(result?.name == "John Doe")
    }

    @Test
    fun testSyncUsers() = runBlocking {
        val remoteUsers = listOf(
            UserRemoteDto(
                id = "1",
                name = "John Doe",
                email = "john@example.com",
                createdAt = System.currentTimeMillis(),
            ),
        )

        whenever(userApi.listUsers()).thenReturn(remoteUsers)

        repository.syncUsers()

        assert(remoteUsers.isNotEmpty())
    }

    @Test
    fun testObserveAllUsers() = runBlocking {
        val users = listOf(
            UserEntity(
                id = "1",
                name = "John Doe",
                email = "john@example.com",
                createdAt = System.currentTimeMillis(),
            ),
        )

        whenever(userDao.observeAllUsers()).thenReturn(flowOf(users))

        repository.observeAllUsers().collect { result ->
            assert(result.size == 1)
            assert(result[0].id == "1")
        }
    }
}
