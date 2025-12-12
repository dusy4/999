package com.ctonew.composemodular.data.network.api

import com.ctonew.composemodular.data.network.models.UserRemoteDto
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApi {

    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: String): UserRemoteDto

    @GET("users")
    suspend fun listUsers(): List<UserRemoteDto>
}
