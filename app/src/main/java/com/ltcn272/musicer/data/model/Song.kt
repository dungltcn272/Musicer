package com.ltcn272.musicer.data.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val duration: Int,
    val thumbnail: String,
    val audioUrl: String,
    val isLocal: Boolean
)
