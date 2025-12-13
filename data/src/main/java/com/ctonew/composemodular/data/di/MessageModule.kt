package com.ctonew.composemodular.data.di

import com.ctonew.composemodular.data.message.AttachmentRepositoryImpl
import com.ctonew.composemodular.data.message.MessageRepositoryImpl
import com.ctonew.composemodular.domain.message.AttachmentRepository
import com.ctonew.composemodular.domain.message.MessageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MessageModule {
    
    @Binds
    @Singleton
    abstract fun bindMessageRepository(impl: MessageRepositoryImpl): MessageRepository
    
    @Binds
    @Singleton
    abstract fun bindAttachmentRepository(impl: AttachmentRepositoryImpl): AttachmentRepository
}
