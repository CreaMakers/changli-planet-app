package com.zhelearn.CSUSTPlanet.feature.common.contract

import com.zhelearn.CSUSTPlanet.core.mvi.MviIntent
import com.zhelearn.CSUSTPlanet.core.mvi.MviSideEffect
import com.zhelearn.CSUSTPlanet.core.mvi.MviViewState
import com.zhelearn.CSUSTPlanet.feature.common.ui.adapter.model.Exam

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
