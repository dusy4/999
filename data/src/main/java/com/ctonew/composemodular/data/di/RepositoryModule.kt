package com.ctonew.composemodular.data.di

import com.ctonew.composemodular.data.repository.MessageRepositoryImpl
import com.ctonew.composemodular.data.repository.PagingMessageRepository
import com.ctonew.composemodular.data.repository.ThreadRepositoryImpl
import com.ctonew.composemodular.data.repository.UserRepositoryImpl
import com.ctonew.composemodular.domain.repository.MessageRepository
import com.ctonew.composemodular.domain.repository.ThreadRepository
import com.ctonew.composemodular.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindThreadRepository(impl: ThreadRepositoryImpl): ThreadRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(impl: MessageRepositoryImpl): MessageRepository

    @Binds
    @Singleton
    abstract fun bindPagingMessageRepository(impl: MessageRepositoryImpl): PagingMessageRepository
}
