package com.ctonew.composemodular.data.message

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ctonew.composemodular.data.db.AppDatabase
import com.ctonew.composemodular.data.db.AttachmentDao
import com.ctonew.composemodular.domain.message.Attachment
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
class AttachmentRepositoryTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    private lateinit var database: AppDatabase
    private lateinit var repository: AttachmentRepositoryImpl
    private lateinit var attachmentDao: AttachmentDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        
        attachmentDao = database.attachmentDao()
        repository = AttachmentRepositoryImpl(attachmentDao)
        
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieveAttachment() = runBlocking {
        val attachment = Attachment(
            id = "att-1",
            messageId = "msg-1",
            url = "https://example.com/image.jpg",
            type = "image/jpeg",
            size = 102400,
            createdAt = System.currentTimeMillis(),
        )
        
        repository.insertAttachment(attachment)
        
        val attachments = repository.getAttachments("msg-1").first()
        assert(attachments.size == 1)
        assert(attachments[0].url == "https://example.com/image.jpg")
    }

    @Test
    fun testDeleteAttachment() = runBlocking {
        val attachment = Attachment(
            id = "att-1",
            messageId = "msg-1",
            url = "https://example.com/image.jpg",
            type = "image/jpeg",
            size = 102400,
            createdAt = System.currentTimeMillis(),
        )
        
        repository.insertAttachment(attachment)
        assert(repository.getAttachments("msg-1").first().size == 1)
        
        repository.deleteAttachment("att-1")
        assert(repository.getAttachments("msg-1").first().isEmpty())
    }

    @Test
    fun testDeleteAttachmentsOlderThan() = runBlocking {
        val now = System.currentTimeMillis()
        val oldTimestamp = now - (31 * 24 * 60 * 60 * 1000L)
        val recentTimestamp = now - (15 * 24 * 60 * 60 * 1000L)
        
        val oldAttachment = Attachment(
            id = "att-old",
            messageId = "msg-1",
            url = "https://example.com/old.jpg",
            type = "image/jpeg",
            size = 102400,
            createdAt = oldTimestamp,
        )
        val recentAttachment = Attachment(
            id = "att-recent",
            messageId = "msg-2",
            url = "https://example.com/recent.jpg",
            type = "image/jpeg",
            size = 102400,
            createdAt = recentTimestamp,
        )
        
        repository.insertAttachment(oldAttachment)
        repository.insertAttachment(recentAttachment)
        
        val cutoff = now - (30 * 24 * 60 * 60 * 1000L)
        repository.deleteAttachmentsOlderThan(cutoff)
        
        val remaining = repository.getAttachments("msg-2").first()
        assert(remaining.size == 1)
        assert(remaining[0].id == "att-recent")
    }
}
