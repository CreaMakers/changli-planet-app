package com.example.changli_planet_app.core.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class MviViewModel<I : MviIntent, S : MviViewState> : ViewModel() {

    private val _state = MutableStateFlow<S>(initialState())
    val state: StateFlow<S> = _state.asStateFlow()

    /**
     * 处理用户意图
     */
    abstract fun processIntent(intent: I)

    /**
     * 提供初始状态
     */
    abstract fun initialState(): S

    protected fun updateState(reducer: S.() -> S) {
        _state.update { it.reducer() }
    }
}