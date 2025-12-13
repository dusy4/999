package com.ctonew.composemodular.data.message

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ctonew.composemodular.data.db.AppDatabase
import com.ctonew.composemodular.data.db.MessageDao
import com.ctonew.composemodular.data.db.OutboundQueueDao
import com.ctonew.composemodular.domain.message.Message
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MessageRepositoryTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    private lateinit var database: AppDatabase
    private lateinit var repository: MessageRepositoryImpl
    private lateinit var messageDao: MessageDao
    private lateinit var queueDao: OutboundQueueDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        
        messageDao = database.messageDao()
        queueDao = database.outboundQueueDao()
        repository = MessageRepositoryImpl(messageDao, queueDao)
        
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieveMessage() = runBlocking {
        val message = Message(
            id = "msg-1",
            conversationId = "conv-1",
            content = "Test message",
            senderId = "user-1",
            timestamp = System.currentTimeMillis(),
            isOutbound = false,
        )
        
        repository.insertMessage(message)
        
        val retrieved = repository.getMessage("msg-1")
        assert(retrieved != null)
        assert(retrieved?.content == "Test message")
    }

    @Test
    fun testGetAllMessagesForConversation() = runBlocking {
        val message1 = Message(
            id = "msg-1",
            conversationId = "conv-1",
            content = "Message 1",
            senderId = "user-1",
            timestamp = System.currentTimeMillis(),
        )
        val message2 = Message(
            id = "msg-2",
            conversationId = "conv-1",
            content = "Message 2",
            senderId = "user-2",
            timestamp = System.currentTimeMillis() + 1000,
        )
        
        repository.insertMessage(message1)
        repository.insertMessage(message2)
        
        val messages = repository.getAllMessages("conv-1").first()
        assert(messages.size == 2)
    }

    @Test
    fun testOutboundQueue() = runBlocking {
        val message = Message(
            id = "msg-1",
            conversationId = "conv-1",
            content = "Outbound message",
            senderId = "user-1",
            timestamp = System.currentTimeMillis(),
            isOutbound = true,
        )
        
        repository.addToOutboundQueue(message)
        
        val queued = repository.getOutboundQueue().first()
        assert(queued.size == 1)
        assert(queued[0].id == "msg-1")
        
        repository.removeFromOutboundQueue("msg-1")
        
        val afterRemoval = repository.getOutboundQueue().first()
        assert(afterRemoval.isEmpty())
    }

    @Test
    fun testDeleteMessage() = runBlocking {
        val message = Message(
            id = "msg-1",
            conversationId = "conv-1",
            content = "Test message",
            senderId = "user-1",
            timestamp = System.currentTimeMillis(),
        )
        
        repository.insertMessage(message)
        assert(repository.getMessage("msg-1") != null)
        
        repository.deleteMessage("msg-1")
        assert(repository.getMessage("msg-1") == null)
    }
}
