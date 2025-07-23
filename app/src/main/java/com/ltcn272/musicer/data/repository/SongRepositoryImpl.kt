package com.ltcn272.musicer.data.repository

import android.content.Context
import com.ltcn272.musicer.data.local.datasource.LocalMusicDataSource
import com.ltcn272.musicer.data.model.Song
import com.ltcn272.musicer.data.remote.datasource.RemoteMusicDataSource
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteMusicDataSource,
    private val localDataSource: LocalMusicDataSource
) : SongRepository {

    override suspend fun getOnlineSongs(): List<Song> {
        return remoteDataSource.fetchTopTracks()
    }

    override suspend fun getLocalSongs(context: Context): List<Song> {
        return localDataSource.getLocalSongs(context)
    }
}
