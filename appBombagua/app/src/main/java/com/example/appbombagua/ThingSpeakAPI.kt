package com.example.appbombagua

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ThingSpeakAPI {
    @GET("channels/2762327/fields/1.json")
    fun getLatestValue(
        @Query("api_key") apiKey: String,
        @Query("results") results: Int = 1
    ): Call<ThingSpeakResponse>
}
