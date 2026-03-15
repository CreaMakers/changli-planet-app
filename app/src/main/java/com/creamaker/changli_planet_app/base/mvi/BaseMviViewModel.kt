package com.creamaker.changli_planet_app.base.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class BaseMviViewModel<Intent : Any, UiState : Any>(
    initialState: UiState
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<UiState> = _state.asStateFlow()

    protected val currentState: UiState
        get() = _state.value

    protected fun setState(reducer: (UiState) -> UiState) {
        _state.update(reducer)
    }

    protected fun setState(newState: UiState) {
        _state.value = newState
    }

    abstract fun process(intent: Intent)
}
