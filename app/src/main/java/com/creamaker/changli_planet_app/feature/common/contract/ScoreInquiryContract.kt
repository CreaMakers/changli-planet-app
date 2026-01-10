package com.creamaker.changli_planet_app.feature.common.contract

import com.creamaker.changli_planet_app.core.mvi.MviIntent
import com.creamaker.changli_planet_app.core.mvi.MviSideEffect
import com.creamaker.changli_planet_app.core.mvi.MviViewState
import com.creamaker.changli_planet_app.feature.common.data.local.entity.Grade

interface ScoreInquiryContract {
    sealed class Intent : MviIntent {
        object InitialLoad : Intent()
        data class RefreshData(val force: Boolean) : Intent()
        data class FetchScoreDetail(val pscjUrl: String, val courseName: String) : Intent()
    }

    data class State(
        val isLoading: Boolean = false,
        val grades: List<Grade> = emptyList()
    ) : MviViewState

    sealed class Effect : MviSideEffect {
        data class ShowToast(val message: String) : Effect()
        data class ShowAuthErrorDialog(val title: String, val msg: String) : Effect()
        data class ShowNetErrorDialog(val title: String, val msg: String) : Effect()
        data class ShowDetailDialog(val detail: String, val title: String) : Effect()
    }
}
