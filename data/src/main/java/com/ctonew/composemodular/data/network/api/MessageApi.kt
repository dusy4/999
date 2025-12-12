package com.ctonew.composemodular.data.network.api

import com.ctonew.composemodular.data.network.models.MessageRemoteDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MessageApi {

    @GET("messages/{id}")
    suspend fun getMessage(@Path("id") messageId: String): MessageRemoteDto

    @GET("threads/{threadId}/messages")
    suspend fun listMessages(
        @Path("threadId") threadId: String,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20,
    ): List<MessageRemoteDto>
}
