package com.ctonew.composemodular.data.db

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ctonew.composemodular.data.db.entities.MessageEntity
import com.ctonew.composemodular.data.db.entities.ThreadEntity
import com.ctonew.composemodular.data.db.entities.UserEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessageDaoTest {

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
    fun testInsertAndRetrieveMessage() = runBlocking {
        val user = UserEntity(
            id = "user-1",
            name = "Test User",
            email = "test@example.com",
            createdAt = System.currentTimeMillis(),
        )
        val thread = ThreadEntity(
            id = "thread-1",
            title = "Test Thread",
            description = "Test",
            userId = "user-1",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        val message = MessageEntity(
            id = "msg-1",
            threadId = "thread-1",
            userId = "user-1",
            content = "Hello World",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )

        database.userDao().upsertUser(user)
        database.threadDao().upsertThread(thread)
        database.messageDao().upsertMessage(message)

        val retrieved = database.messageDao().getMessage("msg-1")

        assert(retrieved != null)
        assert(retrieved?.content == "Hello World")
    }

    @Test
    fun testGetMessagesByThread() = runBlocking {
        val user = UserEntity(
            id = "user-2",
            name = "Test User 2",
            email = "test2@example.com",
            createdAt = System.currentTimeMillis(),
        )
        val thread = ThreadEntity(
            id = "thread-2",
            title = "Test Thread 2",
            description = "Test",
            userId = "user-2",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )

        database.userDao().upsertUser(user)
        database.threadDao().upsertThread(thread)

        val messages = (1..5).map { index ->
            MessageEntity(
                id = "msg-$index",
                threadId = "thread-2",
                userId = "user-2",
                content = "Message $index",
                createdAt = System.currentTimeMillis() + index,
                updatedAt = System.currentTimeMillis() + index,
            )
        }

        database.messageDao().upsertMessages(messages)

        val retrieved = database.messageDao().getMessages("thread-2")

        assert(retrieved.size == 5)
        assert(retrieved[0].content == "Message 5") // DESC order
    }

    @Test
    fun testDeleteMessage() = runBlocking {
        val user = UserEntity(
            id = "user-3",
            name = "Test User 3",
            email = "test3@example.com",
            createdAt = System.currentTimeMillis(),
        )
        val thread = ThreadEntity(
            id = "thread-3",
            title = "Test Thread 3",
            description = "Test",
            userId = "user-3",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        val message = MessageEntity(
            id = "msg-delete",
            threadId = "thread-3",
            userId = "user-3",
            content = "Delete me",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )

        database.userDao().upsertUser(user)
        database.threadDao().upsertThread(thread)
        database.messageDao().upsertMessage(message)
        database.messageDao().deleteMessage("msg-delete")

        val retrieved = database.messageDao().getMessage("msg-delete")

        assert(retrieved == null)
    }
}
