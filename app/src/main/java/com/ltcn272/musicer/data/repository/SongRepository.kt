package com.ltcn272.musicer.data.repository

import android.content.Context
import com.ltcn272.musicer.data.model.Song

interface SongRepository {
    suspend fun getOnlineSongs(): List<Song>
    suspend fun getLocalSongs(context: Context): List<Song>
}
