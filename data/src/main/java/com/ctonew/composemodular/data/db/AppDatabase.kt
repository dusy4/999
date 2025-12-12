package com.ctonew.composemodular.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ctonew.composemodular.data.db.daos.MessageDao
import com.ctonew.composemodular.data.db.daos.ThreadDao
import com.ctonew.composemodular.data.db.daos.UserDao
import com.ctonew.composemodular.data.db.entities.MessageEntity
import com.ctonew.composemodular.data.db.entities.ThreadEntity
import com.ctonew.composemodular.data.db.entities.UserEntity

@Database(
    entities = [
        SampleEntity::class,
        UserEntity::class,
        ThreadEntity::class,
        MessageEntity::class,
        OutboundQueueEntity::class,
        AttachmentEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sampleDao(): SampleDao
    abstract fun userDao(): UserDao
    abstract fun threadDao(): ThreadDao
    abstract fun messageDao(): MessageDao
    abstract fun outboundQueueDao(): OutboundQueueDao
    abstract fun attachmentDao(): AttachmentDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS users (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        email TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS threads (
                        id TEXT PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT,
                        userId TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                database.execSQL("CREATE INDEX IF NOT EXISTS index_threads_userId ON threads(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_threads_createdAt ON threads(createdAt)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_threads_updatedAt ON threads(updatedAt)")

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS messages (
                        id TEXT PRIMARY KEY NOT NULL,
                        threadId TEXT NOT NULL,
                        userId TEXT NOT NULL,
                        content TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        FOREIGN KEY(threadId) REFERENCES threads(id) ON DELETE CASCADE,
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                database.execSQL("CREATE INDEX IF NOT EXISTS index_messages_threadId ON messages(threadId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_messages_userId ON messages(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_messages_createdAt ON messages(createdAt)")
            }
        }
    }
}
