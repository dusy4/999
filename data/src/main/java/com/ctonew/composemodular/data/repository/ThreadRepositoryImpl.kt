package com.ctonew.composemodular.data.repository

import com.ctonew.composemodular.data.db.daos.ThreadDao
import com.ctonew.composemodular.data.mappers.toDomain
import com.ctonew.composemodular.data.mappers.toEntity
import com.ctonew.composemodular.data.network.api.ThreadApi
import com.ctonew.composemodular.domain.models.Thread
import com.ctonew.composemodular.domain.repository.ThreadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ThreadRepositoryImpl @Inject constructor(
    private val threadDao: ThreadDao,
    private val threadApi: ThreadApi,
) : ThreadRepository {

    override fun observeThreads(): Flow<List<Thread>> =
        threadDao.observeAllThreads().map { threads -> threads.map { it.toDomain() } }

    override fun observeThread(threadId: String): Flow<Thread?> =
        threadDao.observeThread(threadId).map { it?.toDomain() }

    override suspend fun getThreads(): List<Thread> =
        threadDao.getAllThreads().map { it.toDomain() }

    override suspend fun getThread(threadId: String): Thread? =
        threadDao.getThread(threadId)?.toDomain()

    override suspend fun upsertThread(thread: Thread) {
        threadDao.upsertThread(thread.toEntity())
    }

    override suspend fun deleteThread(threadId: String) {
        threadDao.deleteThread(threadId)
    }

    override suspend fun syncThreads() {
        try {
            val remoteThreads = threadApi.listThreads()
            val localThreads = remoteThreads.map { it.toEntity() }
            threadDao.upsertThreads(localThreads)
        } catch (e: Exception) {
            // Log error but don't fail - use cached data
        }
    }
}
