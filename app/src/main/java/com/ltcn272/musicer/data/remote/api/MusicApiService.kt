package com.ltcn272.musicer.data.remote.api

import com.ltcn272.musicer.data.remote.model.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicApiService {
    @GET("chart/0/tracks")
    suspend fun getTopTracks(
        @Query("limit") limit: Int = 30
    ): ApiResponse
}
