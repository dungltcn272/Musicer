package com.ltcn272.musicer.screen.main.assets

import android.content.Context
import com.ltcn272.musicer.data.repository.source.local.LocalMusicDataSource
import javax.inject.Inject

class AssetsPresenter @Inject constructor(
    private val localMusicDataSource: LocalMusicDataSource
) : AssetsContract.Presenter {

    private var view: AssetsContract.View? = null

    override fun attachView(view: AssetsContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override fun loadSongsFromDevice(context: Context) {
        val songs = localMusicDataSource.getLocalSongs(context)
        if (songs.isNotEmpty()) {
            view?.showSongs(songs)
        } else {
            view?.showError("Không tìm thấy bài hát nào trên thiết bị")
        }
    }
}
