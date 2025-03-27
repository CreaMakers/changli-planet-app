package com.example.changli_planet_app.Activity.Contract

import com.example.changli_planet_app.Core.MVI.MviIntent
import com.example.changli_planet_app.Core.MVI.MviViewModel
import com.example.changli_planet_app.Core.MVI.MviViewState
import com.example.changli_planet_app.Network.Resource
import com.example.changli_planet_app.Network.Response.FreshNewsItem

class FreshNewsContract {
    sealed class Intent : MviIntent {
        class UpdateTabIndex(val currentIndex: Int) : Intent()
        class Initialization(): Intent()
    }

    data class State(
        var currentTab: Int,
        var freshNewsList: Resource<List<FreshNewsItem>>
    ) : MviViewState

    sealed class Event {
        object showOverlay : Event()
    }
}