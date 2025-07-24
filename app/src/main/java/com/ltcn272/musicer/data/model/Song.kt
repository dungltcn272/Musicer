package com.ltcn272.musicer.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val duration: Int,
    val thumbnail: String,
    val audioUrl: String,
    val isLocal: Boolean
) : Parcelable
