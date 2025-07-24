package com.ltcn272.musicer.data.repository.source.remote

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    val data: List<ApiSong>
)

data class ApiSong(
    val id: Long,
    val title: String,
    val duration: Int,
    val preview: String,
    val artist: ApiArtist,
    val album: ApiAlbum
)

data class ApiArtist(
    val id: Long,
    val name: String,
    val picture: String,
    val picture_medium: String
)

data class ApiAlbum(
    val id: Long,
    val title: String,
    @SerializedName("cover_medium")
    val coverMedium: String
)
