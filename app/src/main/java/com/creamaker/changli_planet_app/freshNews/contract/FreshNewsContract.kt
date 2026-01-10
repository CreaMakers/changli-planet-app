package com.creamaker.changli_planet_app.freshNews.contract

import com.creamaker.changli_planet_app.core.mvi.MviIntent
import com.creamaker.changli_planet_app.core.mvi.MviViewState
import com.creamaker.changli_planet_app.core.network.ApiResponse
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.FreshNewsItem
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.FreshNewsItemResult
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.FreshNewsPublish
import java.io.File

class FreshNewsContract {
    sealed class Intent : MviIntent {
        class InputMessage(val value: Any, val type: String) : Intent()
        class AddImage(val file: File) : Intent()
        class RemoveImage(val index: Int) : Intent()
        class Publish : Intent()
        class LoadIp: Intent()
        class ClearAll : Intent()
        class RefreshNewsByTime(val page: Int, val pageSize: Int) : Intent()
        class UpdateTabIndex(val currentIndex: Int) : Intent()
        class Initialization : Intent()
        class UpdateUserProfile(val userId: Int) : Intent()

        class LikeNews(val freshNewsId: Int) : Intent()
        class FavoriteNews(val freshNewsId: Int) : Intent()
        class OpenComments(val freshNewsItem: FreshNewsItem) : Intent()
        class UpdateLocalCommentCount(val newsId: Int, val count: Int) : Intent()
        class UpdateLocalUserInfo(val userId: Int, val name: String, val avatar: String) : Intent()
    }

    data class State(
        var currentTab: Int,
        var freshNewsListResults: ApiResponse<List<FreshNewsItemResult>>,
        var publishNews: FreshNewsPublish,
        var images: MutableList<File>,
        var isEnable: Boolean,
        var page: Int
    ) : MviViewState

    sealed class Event {
        object showOverlay : Event()
        object closePublish : Event()
        object RefreshNewsList : Event()
        object openComments : Event()
    }

}