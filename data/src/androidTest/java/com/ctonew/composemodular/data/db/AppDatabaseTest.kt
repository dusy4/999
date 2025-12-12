package com.ctonew.composemodular.data.db

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ctonew.composemodular.data.db.entities.UserEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieveUser() = runBlocking {
        val user = UserEntity(
            id = "test-1",
            name = "Test User",
            email = "test@example.com",
            createdAt = System.currentTimeMillis(),
        )

        database.userDao().upsertUser(user)
        val retrieved = database.userDao().getUser("test-1")

        assert(retrieved != null)
        assert(retrieved?.name == "Test User")
    }

    @Test
    fun testUpsertMultipleUsers() = runBlocking {
        val users = listOf(
            UserEntity(
                id = "1",
                name = "User 1",
                email = "user1@example.com",
                createdAt = System.currentTimeMillis(),
            ),
            UserEntity(
                id = "2",
                name = "User 2",
                email = "user2@example.com",
                createdAt = System.currentTimeMillis() + 1000,
            ),
        )

        database.userDao().upsertUsers(users)
        val retrieved = database.userDao().getAllUsers()

        assert(retrieved.size == 2)
    }

    @Test
    fun testDeleteUser() = runBlocking {
        val user = UserEntity(
            id = "test-delete",
            name = "Delete Test",
            email = "delete@example.com",
            createdAt = System.currentTimeMillis(),
        )

        database.userDao().upsertUser(user)
        database.userDao().deleteUser("test-delete")
        val retrieved = database.userDao().getUser("test-delete")

        assert(retrieved == null)
    }
}
