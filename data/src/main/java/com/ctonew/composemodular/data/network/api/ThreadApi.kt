package com.ctonew.composemodular.data.network.api

import com.ctonew.composemodular.data.network.models.ThreadRemoteDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ThreadApi {

    @GET("threads/{id}")
    suspend fun getThread(@Path("id") threadId: String): ThreadRemoteDto

    @GET("threads")
    suspend fun listThreads(): List<ThreadRemoteDto>
}
