package com.ltcn272.musicer.screen.main.assets

import android.content.Context
import com.ltcn272.musicer.data.model.Song

interface AssetsContract {
    interface View {
        fun showSongs(songs: List<Song>)
        fun showError(message: String)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadSongsFromDevice(context: Context)
    }
}
