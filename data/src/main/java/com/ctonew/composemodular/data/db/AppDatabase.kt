package com.ctonew.composemodular.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ctonew.composemodular.data.db.converters.TypeConverters
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
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sampleDao(): SampleDao
    abstract fun userDao(): UserDao
    abstract fun threadDao(): ThreadDao
    abstract fun messageDao(): MessageDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS users (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        email TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        avatarUrl TEXT NOT NULL DEFAULT '',
                        username TEXT NOT NULL DEFAULT '',
                        bio TEXT NOT NULL DEFAULT '',
                        isFollowing INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS threads (
                        id TEXT PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT,
                        userId TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        content TEXT NOT NULL DEFAULT '',
                        mediaUrls TEXT,
                        replyCount INTEGER NOT NULL DEFAULT 0,
                        likeCount INTEGER NOT NULL DEFAULT 0,
                        isLiked INTEGER NOT NULL DEFAULT 0,
                        repostCount INTEGER NOT NULL DEFAULT 0,
                        isReposted INTEGER NOT NULL DEFAULT 0,
                        parentThreadId TEXT,
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                db.execSQL("CREATE INDEX IF NOT EXISTS index_threads_userId ON threads(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_threads_createdAt ON threads(createdAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_threads_updatedAt ON threads(updatedAt)")

                db.execSQL("""
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

                db.execSQL("CREATE INDEX IF NOT EXISTS index_messages_threadId ON messages(threadId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_messages_userId ON messages(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_messages_createdAt ON messages(createdAt)")
            }
        }
    }
}
