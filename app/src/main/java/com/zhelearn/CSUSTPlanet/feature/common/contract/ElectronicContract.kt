package com.zhelearn.CSUSTPlanet.feature.common.contract

import com.zhelearn.CSUSTPlanet.core.mvi.MviIntent
import com.zhelearn.CSUSTPlanet.core.mvi.MviSideEffect
import com.zhelearn.CSUSTPlanet.core.mvi.MviViewState

interface ElectronicContract {
    sealed class Intent : MviIntent {
        data class SelectSchool(val address: String) : Intent()
        data class SelectDorm(val buildId: String) : Intent()
        data class QueryElectricity(val address: String, val buildId: String, val nod: String) :
            Intent()

        data class Init(val address: String, val buildId: String, val nod: String) : Intent()
    }

    data class State(
        val address: String = "选择校区",
        val buildId: String = "选择宿舍楼",
        val elec: String = "",
        val isElec: Boolean = false,
        val isLoading: Boolean = false
    ) : MviViewState

    sealed class Effect : MviSideEffect {
        data class UpdateWidget(val appWidgetIds: IntArray) :
            Effect() // Optional: logic to update widget might be in Activity, but effect can trigger it

        data class ShowToast(val message: String) : Effect()
    }
}
