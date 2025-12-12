package com.ctonew.composemodular.data.di

import android.content.Context
import androidx.room.Room
import com.ctonew.composemodular.data.db.AppDatabase
import com.ctonew.composemodular.data.db.daos.MessageDao
import com.ctonew.composemodular.data.db.daos.ThreadDao
import com.ctonew.composemodular.data.db.daos.UserDao
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
    ).addMigrations(AppDatabase.MIGRATION_1_2).build()

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideThreadDao(database: AppDatabase): ThreadDao = database.threadDao()

    @Provides
    @Singleton
    fun provideMessageDao(database: AppDatabase): MessageDao = database.messageDao()
}
