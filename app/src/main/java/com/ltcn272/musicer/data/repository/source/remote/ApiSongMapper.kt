package com.ltcn272.musicer.data.repository.source.remote

import com.ltcn272.musicer.data.model.Song

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
