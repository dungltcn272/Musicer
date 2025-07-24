package com.ltcn272.musicer.data.repository.source.remote

import com.ltcn272.musicer.data.model.Song
import javax.inject.Inject

class RemoteMusicDataSource @Inject constructor(
    private val apiService: MusicApiService
) {

    suspend fun fetchTopTracks(): Result<List<Song>> {
        return try {
            val response = apiService.getTopTracks()

            val songs = response.data.map { it.toDomainModel() }

            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}