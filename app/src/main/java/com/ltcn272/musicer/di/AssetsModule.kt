package com.ltcn272.musicer.di

import com.ltcn272.musicer.screen.main.assets.AssetsContract
import com.ltcn272.musicer.screen.main.assets.AssetsPresenter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
abstract class AssetsModule {

    @Binds
    abstract fun bindAssetsPresenter(
        presenter: AssetsPresenter
    ): AssetsContract.Presenter
}
