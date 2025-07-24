package com.ltcn272.musicer.data.repository.source.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface MusicApiService {
    @GET("chart/0/tracks")
    suspend fun getTopTracks(
        @Query("limit") limit: Int = 30
    ): ApiResponse
}
