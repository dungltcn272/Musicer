package com.ltcn272.musicer.di

import com.ltcn272.musicer.data.local.datasource.LocalMusicDataSource
import com.ltcn272.musicer.data.remote.api.MusicApiService
import com.ltcn272.musicer.data.remote.datasource.RemoteMusicDataSource
import com.ltcn272.musicer.data.repository.SongRepository
import com.ltcn272.musicer.data.repository.SongRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object MusicModule {

    @Provides
    fun provideMusicApiService(): MusicApiService = Retrofit.Builder()
        .baseUrl("https://api.deezer.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MusicApiService::class.java)

    @Provides
    fun provideRemoteMusicDataSource(
        apiService: MusicApiService
    ) = RemoteMusicDataSource(apiService)

    @Provides
    fun provideLocalMusicDataSource() = LocalMusicDataSource()

    @Provides
    fun provideSongRepository(
        remote: RemoteMusicDataSource,
        local: LocalMusicDataSource
    ): SongRepository = SongRepositoryImpl(remote, local)

}