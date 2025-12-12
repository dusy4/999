package com.ctonew.composemodular.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SampleEntity::class,
        MessageEntity::class,
        OutboundQueueEntity::class,
        AttachmentEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sampleDao(): SampleDao
    abstract fun messageDao(): MessageDao
    abstract fun outboundQueueDao(): OutboundQueueDao
    abstract fun attachmentDao(): AttachmentDao
}
