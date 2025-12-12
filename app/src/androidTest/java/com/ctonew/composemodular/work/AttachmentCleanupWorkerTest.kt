package com.ctonew.composemodular.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.ctonew.composemodular.data.work.AttachmentCleanupWorker
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class AttachmentCleanupWorkerTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        hiltRule.inject()
    }

    @Test
    fun testAttachmentCleanupWorkerSucceeds() = runBlocking {
        val worker = TestListenableWorkerBuilder<AttachmentCleanupWorker>(
            context = context,
            inputData = androidx.work.Data.Builder()
                .putInt(AttachmentCleanupWorker.KEY_RETENTION_DAYS, 30)
                .build(),
        ).build()

        val result = worker.doWork()
        
        assert(result is ListenableWorker.Result.Success)
    }

    @Test
    fun testAttachmentCleanupWorkerWithDefaultRetention() = runBlocking {
        val worker = TestListenableWorkerBuilder<AttachmentCleanupWorker>(
            context = context,
        ).build()

        val result = worker.doWork()
        
        assert(result is ListenableWorker.Result.Success)
    }
}
