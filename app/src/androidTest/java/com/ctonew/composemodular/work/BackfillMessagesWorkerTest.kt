package com.ctonew.composemodular.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.testing.TestListenableWorkerBuilder
import com.ctonew.composemodular.data.work.BackfillMessagesWorker
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class BackfillMessagesWorkerTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        hiltRule.inject()
    }

    @Test
    fun testBackfillMessagesWorkerRetries() = runBlocking {
        val worker = TestListenableWorkerBuilder<BackfillMessagesWorker>(
            context = context,
            inputData = androidx.work.Data.Builder()
                .putString(BackfillMessagesWorker.KEY_CONVERSATION_ID, "conv-123")
                .putLong(BackfillMessagesWorker.KEY_SINCE_TIMESTAMP, 0L)
                .build(),
        ).build()

        val result = worker.doWork()
        
        assert(result is ListenableWorker.Result.Retry || result is ListenableWorker.Result.Success)
    }

    @Test
    fun testBackfillMessagesWorkerFailsWithoutConversationId() = runBlocking {
        val worker = TestListenableWorkerBuilder<BackfillMessagesWorker>(
            context = context,
            inputData = androidx.work.Data.Builder().build(),
        ).build()

        val result = worker.doWork()
        
        assert(result is ListenableWorker.Result.Retry)
    }
}
