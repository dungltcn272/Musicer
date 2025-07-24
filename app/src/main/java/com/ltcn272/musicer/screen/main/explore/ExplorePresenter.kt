package com.ltcn272.musicer.screen.main.explore

import com.ltcn272.musicer.data.repository.SongRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExplorePresenter @Inject constructor(
    private val songRepository: SongRepository
) : ExploreContract.Presenter {

    private var view: ExploreContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + Job())

    override fun attachView(view: ExploreContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override fun loadOnlineSongs() {
        view?.showLoading()
        presenterScope.launch {
            val result = songRepository.getOnlineSongs()
            view?.hideLoading()

            result.onSuccess { songs ->
                view?.showOnlineSongs(songs)
            }.onFailure { error ->
                view?.showError(error.message ?: "Đã xảy ra lỗi khi tải nhạc online")
            }
        }
    }
}
