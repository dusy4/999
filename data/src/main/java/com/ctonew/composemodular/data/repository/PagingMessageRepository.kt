package com.ctonew.composemodular.data.repository

import androidx.paging.PagingData
import com.ctonew.composemodular.domain.models.Message
import kotlinx.coroutines.flow.Flow

interface PagingMessageRepository {
    fun observeMessagesPaged(threadId: String): Flow<PagingData<Message>>
}
