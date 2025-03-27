package com.example.changli_planet_app.Activity.ViewModel

import androidx.lifecycle.viewModelScope
import com.example.changli_planet_app.Activity.Contract.FreshNewsContract
import com.example.changli_planet_app.Core.CoroutineContext.ErrorCoroutineContext
import com.example.changli_planet_app.Core.MVI.MviViewModel
import com.example.changli_planet_app.Network.Resource
import kotlinx.coroutines.launch

class FreshNewsViewModel : MviViewModel<FreshNewsContract.Intent, FreshNewsContract.State>() {

    override fun processIntent(intent: FreshNewsContract.Intent) {
        when (intent) {
            is FreshNewsContract.Intent.UpdateTabIndex -> {
                changeCurrentTab(intent.currentIndex)
            }

            is FreshNewsContract.Intent.Initialization -> {

            }
        }
    }

    // 给State初始值
    override fun initialState() = FreshNewsContract.State(
        0,
        Resource.Loading()
    )

    private fun changeCurrentTab(currentTab: Int) {
        viewModelScope.launch(ErrorCoroutineContext()) {

        }
        updateState {
            copy(currentTab = currentTab)
        }
    }
}