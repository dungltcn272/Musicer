package com.ltcn272.musicer.di

import com.ltcn272.musicer.screen.main.assets.AssetsContract
import com.ltcn272.musicer.screen.main.assets.AssetsPresenter
import com.ltcn272.musicer.screen.main.explore.ExploreContract
import com.ltcn272.musicer.screen.main.explore.ExplorePresenter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
abstract class PresenterModule {

    @Binds
    abstract fun bindAssetsPresenter(
        assetsPresenter: AssetsPresenter
    ): AssetsContract.Presenter

    @Binds
    abstract fun bindExplorePresenter(
        explorePresenter: ExplorePresenter
    ) : ExploreContract.Presenter
}
