package com.creamaker.changli_planet_app.feature.common.contract

import com.creamaker.changli_planet_app.core.mvi.MviIntent
import com.creamaker.changli_planet_app.core.mvi.MviSideEffect
import com.creamaker.changli_planet_app.core.mvi.MviViewState
import com.creamaker.changli_planet_app.feature.common.ui.adapter.model.Exam

interface ExamArrangementContract {
    sealed class Intent : MviIntent {
        data class LoadExamArrangement(val termTime: String) : Intent()
    }

    data class State(
        val exams: List<Exam> = emptyList(),
        val isLoading: Boolean = false
    ) : MviViewState

    sealed class Effect : MviSideEffect {
        data class ShowToast(val message: String) : Effect()
        data class ShowErrorDialog(val message: String) : Effect()
    }
}
