package com.ltcn272.musicer.data.remote.datasource

import com.ltcn272.musicer.data.model.Song
import com.ltcn272.musicer.data.remote.api.MusicApiService
import com.ltcn272.musicer.data.remote.mapper.toDomainModel
import javax.inject.Inject

class RemoteMusicDataSource @Inject constructor(
    private val apiService: MusicApiService
) {
    suspend fun fetchTopTracks(): List<Song> {
        val response = apiService.getTopTracks()
        return response.data.map {
            it.toDomainModel()
        }
    }
}
