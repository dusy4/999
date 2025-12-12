package com.ctonew.composemodular.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC")
    fun getMessages(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: String)
}

@Dao
interface OutboundQueueDao {
    @Query("SELECT m.* FROM messages m INNER JOIN outbound_queue q ON m.id = q.messageId ORDER BY q.addedAt ASC")
    fun getQueuedMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM outbound_queue WHERE messageId = :messageId")
    suspend fun getQueueEntry(messageId: String): OutboundQueueEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(entry: OutboundQueueEntity)

    @Query("DELETE FROM outbound_queue WHERE messageId = :messageId")
    suspend fun dequeue(messageId: String)

    @Update
    suspend fun updateQueueEntry(entry: OutboundQueueEntity)
}
