package com.ctonew.composemodular.data.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ctonew.composemodular.data.db.entities.ThreadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreadDao {

    @Query("SELECT * FROM threads ORDER BY updatedAt DESC")
    fun observeAllThreads(): Flow<List<ThreadEntity>>

    @Query("SELECT * FROM threads WHERE id = :threadId")
    fun observeThread(threadId: String): Flow<ThreadEntity?>

    @Query("SELECT * FROM threads ORDER BY updatedAt DESC")
    suspend fun getAllThreads(): List<ThreadEntity>

    @Query("SELECT * FROM threads WHERE id = :threadId")
    suspend fun getThread(threadId: String): ThreadEntity?

    @Query("SELECT * FROM threads WHERE userId = :userId ORDER BY updatedAt DESC")
    suspend fun getThreadsByUser(userId: String): List<ThreadEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertThread(thread: ThreadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertThreads(threads: List<ThreadEntity>)

    @Query("DELETE FROM threads WHERE id = :threadId")
    suspend fun deleteThread(threadId: String)

    @Query("DELETE FROM threads")
    suspend fun deleteAllThreads()
}
