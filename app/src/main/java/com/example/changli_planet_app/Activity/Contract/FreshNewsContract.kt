package com.example.changli_planet_app.Activity.Contract

import com.example.changli_planet_app.Core.MVI.MviIntent
import com.example.changli_planet_app.Core.MVI.MviViewModel
import com.example.changli_planet_app.Core.MVI.MviViewState
import com.example.changli_planet_app.Network.Resource
import com.example.changli_planet_app.Network.Response.FreshNews
import com.example.changli_planet_app.Network.Response.FreshNewsItem
import com.example.changli_planet_app.Network.Response.FreshNews_Publish
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
    }

    data class State(
        var currentTab: Int,
        var freshNewsList: Resource<List<FreshNewsItem>>,
        var publishNews: FreshNews_Publish,
        var images: MutableList<File>,
        var isEnable: Boolean,
        var page: Int,
    ) : MviViewState

    sealed class Event {
        object showOverlay : Event()
        object closePublish : Event()
        object RefreshNewsList : FreshNewsContract.Event()
    }

}