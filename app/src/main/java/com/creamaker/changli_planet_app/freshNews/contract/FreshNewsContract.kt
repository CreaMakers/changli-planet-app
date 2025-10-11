package com.creamaker.changli_planet_app.freshNews.contract

import com.creamaker.changli_planet_app.core.mvi.MviIntent
import com.creamaker.changli_planet_app.core.mvi.MviViewState
import com.creamaker.changli_planet_app.core.network.Resource
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.FreshNewsItem
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.FreshNewsPublish
import java.io.File

class FreshNewsContract {
    sealed class Intent : MviIntent {
        class InputMessage(val value: Any, val type: String) : Intent()
        class AddImage(val file: File) : Intent()
        class RemoveImage(val index: Int) : Intent()
        class Publish : Intent()
        class ClearAll : Intent()
        class RefreshNewsByTime(val page: Int, val pageSize: Int) : Intent()
        class UpdateTabIndex(val currentIndex: Int) : Intent()
        class Initialization : Intent()
        class UpdateUserProfile(val userId: Int) : Intent()

        class LikeNews(val freshNewsItem: FreshNewsItem) : Intent()
        class FavoriteNews(val freshNewsItem: FreshNewsItem) : Intent()
    }

    data class State(
        var currentTab: Int,
        var freshNewsList: Resource<List<FreshNewsItem>>,
        var publishNews: FreshNewsPublish,
        var images: MutableList<File>,
        var isEnable: Boolean,
        var page: Int,
    ) : MviViewState

    sealed class Event {
        object showOverlay : Event()
        object closePublish : Event()
        object RefreshNewsList : Event()
    }

}