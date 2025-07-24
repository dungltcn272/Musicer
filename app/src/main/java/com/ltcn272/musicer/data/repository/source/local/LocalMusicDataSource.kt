package com.ltcn272.musicer.data.repository.source.local

import android.content.Context
import android.provider.MediaStore
import com.ltcn272.musicer.data.model.Song
import javax.inject.Inject


class LocalMusicDataSource @Inject constructor() {

    fun getLocalSongs(context: Context): List<Song> {
        val contentResolver = context.contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        val cursor = contentResolver.query(uri, null, selection, null, sortOrder)

        val songs = mutableListOf<Song>()

        cursor?.use {
            val titleColumn = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val durationColumn = it.getColumnIndex(MediaStore.Audio.Media.DURATION)
            val dataColumn = it.getColumnIndex(MediaStore.Audio.Media.DATA)

            while (it.moveToNext()) {
                val title = it.getString(titleColumn)
                val artist = it.getString(artistColumn)
                val duration = it.getInt(durationColumn)
                val data = it.getString(dataColumn)

                songs.add(
                    Song(
                        id = data,
                        title = title,
                        artist = artist,
                        duration = duration,
                        thumbnail = "", // tạm không có ảnh
                        audioUrl = data,
                        isLocal = true
                    )
                )
            }
        }
        return songs
    }
}
