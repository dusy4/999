package com.ctonew.composemodular.data.db.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ctonew.composemodular.data.db.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE threadId = :threadId ORDER BY createdAt DESC")
    fun observeMessagesPaged(threadId: String): PagingSource<Int, MessageEntity>

    @Query("SELECT * FROM messages WHERE threadId = :threadId ORDER BY createdAt DESC")
    fun observeMessages(threadId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE threadId = :threadId ORDER BY createdAt DESC")
    suspend fun getMessages(threadId: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessage(messageId: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE threadId = :threadId ORDER BY createdAt DESC LIMIT :pageSize OFFSET :offset")
    suspend fun getMessagesPaged(threadId: String, offset: Int, pageSize: Int): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessages(messages: List<MessageEntity>)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM messages WHERE threadId = :threadId")
    suspend fun deleteMessagesByThread(threadId: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}
