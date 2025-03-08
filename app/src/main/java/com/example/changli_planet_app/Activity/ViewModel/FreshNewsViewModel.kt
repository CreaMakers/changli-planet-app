package com.example.changli_planet_app.Activity.ViewModel

import com.example.changli_planet_app.Activity.Contract.FreshNewsContract
import com.example.changli_planet_app.Core.MVI.MviViewModel
import com.example.changli_planet_app.Network.Resource

class FreshNewsViewModel : MviViewModel<FreshNewsContract.Intent, FreshNewsContract.State>() {
    override fun processIntent(intent: FreshNewsContract.Intent) {

    }

    // 给State初始值
    override fun initialState() = FreshNewsContract.State(
        0,
        Resource.Loading()
    )

    private fun changeCurrentTab(currentTab: Int) {
        updateState {
            copy(currentTab = currentTab)
        }
    }
}