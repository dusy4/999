package com.ctonew.composemodular.data.di

import android.content.Context
import androidx.room.Room
import com.ctonew.composemodular.data.db.AppDatabase
import com.ctonew.composemodular.data.db.daos.MessageDao
import com.ctonew.composemodular.data.db.daos.ThreadDao
import com.ctonew.composemodular.data.db.daos.UserDao
import com.ctonew.composemodular.data.db.entities.ThreadEntity
import com.ctonew.composemodular.data.db.entities.UserEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "compose_modular_db",
    ).addMigrations(AppDatabase.MIGRATION_1_2)
        .addCallback(
            object : androidx.room.RoomDatabase.Callback() {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onCreate(db)
                    populateSampleData(context)
                }
            }
        )
        .build()

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideThreadDao(database: AppDatabase): ThreadDao = database.threadDao()

    @Provides
    @Singleton
    fun provideMessageDao(database: AppDatabase): MessageDao = database.messageDao()

    private fun populateSampleData(context: Context) {
        val database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "compose_modular_db",
        ).addMigrations(AppDatabase.MIGRATION_1_2).build()

        val now = System.currentTimeMillis()

        val users = listOf(
            UserEntity(
                id = "user1",
                name = "Sarah Chen",
                email = "sarah@example.com",
                createdAt = now - 86400000 * 30,
                avatarUrl = "https://i.pravatar.cc/150?img=1",
                username = "sarahchen",
                bio = "Software engineer, coffee lover ☕"
            ),
            UserEntity(
                id = "user2",
                name = "Alex Rivera",
                email = "alex@example.com",
                createdAt = now - 86400000 * 25,
                avatarUrl = "https://i.pravatar.cc/150?img=2",
                username = "alexrivera",
                bio = "Product designer | Design systems enthusiast"
            ),
            UserEntity(
                id = "user3",
                name = "Jordan Kim",
                email = "jordan@example.com",
                createdAt = now - 86400000 * 20,
                avatarUrl = "https://i.pravatar.cc/150?img=3",
                username = "jordankim",
                bio = "Photographer | Filmmaker | Storyteller"
            ),
            UserEntity(
                id = "user4",
                name = "Taylor Tech",
                email = "taylor@example.com",
                createdAt = now - 86400000 * 15,
                avatarUrl = "https://i.pravatar.cc/150?img=4",
                username = "taylortech",
                bio = "Tech lead at StartupX | Open source maintainer"
            ),
        )

        val threads = listOf(
            ThreadEntity(
                id = "thread1",
                title = "Excited to announce",
                description = "Excited to announce that I've just started a new role as Senior Engineer!",
                userId = "user1",
                createdAt = now - 3600000,
                updatedAt = now - 3600000,
                content = "Excited to announce that I've just started a new role as Senior Software Engineer! Looking forward to working with an amazing team and building products that matter. Let's go! 🚀",
                mediaUrls = listOf("https://images.unsplash.com/photo-1552664730-d307ca884978?w=500&h=300&fit=crop"),
                replyCount = 24,
                likeCount = 156,
                isLiked = false,
                repostCount = 12,
                isReposted = false,
                parentThreadId = null
            ),
            ThreadEntity(
                id = "thread2",
                title = "Design thinking",
                description = "Just wrapped up an amazing design workshop",
                userId = "user2",
                createdAt = now - 7200000,
                updatedAt = now - 7200000,
                content = "Just wrapped up an amazing design workshop with my team. We explored new approaches to solving complex user problems. The discussions were invaluable! 🎨✨",
                mediaUrls = listOf("https://images.unsplash.com/photo-1561070791-2526d30994b5?w=500&h=300&fit=crop"),
                replyCount = 18,
                likeCount = 89,
                isLiked = false,
                repostCount = 7,
                isReposted = false,
                parentThreadId = null
            ),
            ThreadEntity(
                id = "thread3",
                title = "Beautiful sunset",
                description = "Captured this beautiful moment today",
                userId = "user3",
                createdAt = now - 10800000,
                updatedAt = now - 10800000,
                content = "Captured this beautiful moment during golden hour. Nature never ceases to amaze me. 📸🌅",
                mediaUrls = listOf(
                    "https://images.unsplash.com/photo-1495612411223-4d71bcdd2085?w=500&h=300&fit=crop",
                    "https://images.unsplash.com/photo-1495567720989-cebdbdd97913?w=500&h=300&fit=crop"
                ),
                replyCount = 42,
                likeCount = 312,
                isLiked = false,
                repostCount = 34,
                isReposted = false,
                parentThreadId = null
            ),
            ThreadEntity(
                id = "thread4",
                title = "Open source milestone",
                description = "Our project just hit 10k stars",
                userId = "user4",
                createdAt = now - 14400000,
                updatedAt = now - 14400000,
                content = "🎉 Our open source project just hit 10,000 stars on GitHub! This wouldn't have been possible without our amazing community. Thank you all! 💜 #OpenSource #DevCommunity",
                mediaUrls = emptyList(),
                replyCount = 87,
                likeCount = 523,
                isLiked = false,
                repostCount = 156,
                isReposted = false,
                parentThreadId = null
            ),
            ThreadEntity(
                id = "thread5",
                title = "React patterns",
                description = "Deep dive into React patterns",
                userId = "user1",
                createdAt = now - 18000000,
                updatedAt = now - 18000000,
                content = "Just published a comprehensive guide on advanced React patterns. Covering hooks, context, and performance optimization. Check it out! 📚",
                mediaUrls = emptyList(),
                replyCount = 56,
                likeCount = 278,
                isLiked = false,
                repostCount = 45,
                isReposted = false,
                parentThreadId = null
            ),
            ThreadEntity(
                id = "thread6",
                title = "Figma tips and tricks",
                description = "Share useful Figma shortcuts",
                userId = "user2",
                createdAt = now - 21600000,
                updatedAt = now - 21600000,
                content = "Share your favorite Figma tips and tricks! Here are mine: 1) Use @ to access component instances 2) Shift+2 for measurement tool 3) Smart select with Shift+click What's yours? 🎯",
                mediaUrls = emptyList(),
                replyCount = 67,
                likeCount = 145,
                isLiked = false,
                repostCount = 23,
                isReposted = false,
                parentThreadId = null
            ),
        )

        try {
            val userDao = database.userDao()
            val threadDao = database.threadDao()

            userDao.upsertUsers(users)
            threadDao.upsertThreads(threads)
        } catch (e: Exception) {
            // Database might already have data
        }

        database.close()
    }
}
