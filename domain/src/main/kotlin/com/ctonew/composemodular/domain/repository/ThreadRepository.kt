package com.ctonew.composemodular.domain.repository

import com.ctonew.composemodular.domain.models.Thread
import kotlinx.coroutines.flow.Flow

interface ThreadRepository {
    fun observeThreads(): Flow<List<Thread>>
    fun observeThread(threadId: String): Flow<Thread?>
    suspend fun getThreads(): List<Thread>
    suspend fun getThread(threadId: String): Thread?
    suspend fun upsertThread(thread: Thread)
    suspend fun deleteThread(threadId: String)
    suspend fun syncThreads()
}
