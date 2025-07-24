package com.ltcn272.musicer.screen.main.explore

import com.ltcn272.musicer.data.model.Song

interface ExploreContract {
    interface View {
        fun showOnlineSongs(songs: List<Song>)
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadOnlineSongs()
    }
}
