package com.ltcn272.musicer.data.remote.mapper

import com.ltcn272.musicer.data.model.Song
import com.ltcn272.musicer.data.remote.model.ApiSong

fun ApiSong.toDomainModel(): Song {
    return Song(
        id = id.toString(),
        title = title,
        artist = artist.name,
        duration = duration,
        thumbnail = album.coverMedium,
        audioUrl = preview,
        isLocal = false
    )
}
