package com.ctonew.composemodular.data.network

import retrofit2.http.GET

interface SampleApi {

    @GET("status")
    suspend fun status(): String
}
