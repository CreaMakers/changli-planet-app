package com.creamaker.changli_planet_app.common.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class DownloadStatus {
    IDLE,
    STARTED,
    PROGRESS,
    SUCCESS,
    FAILED
}

data class DownloadProgressState(
    val status: DownloadStatus = DownloadStatus.IDLE,
    val progress: Int = 0
)

object DownloadProgressStore {
    private val _state = MutableStateFlow(DownloadProgressState())
    val state: StateFlow<DownloadProgressState> = _state.asStateFlow()

    fun markStarted() {
        _state.value = DownloadProgressState(status = DownloadStatus.STARTED, progress = 0)
    }

    fun markProgress(progress: Int) {
        _state.value = DownloadProgressState(
            status = DownloadStatus.PROGRESS,
            progress = progress.coerceIn(0, 100)
        )
    }

    fun markSuccess() {
        _state.value = DownloadProgressState(status = DownloadStatus.SUCCESS, progress = 100)
    }

    fun markFailed(progress: Int) {
        _state.value = DownloadProgressState(
            status = DownloadStatus.FAILED,
            progress = progress.coerceIn(0, 100)
        )
    }

    fun reset() {
        _state.value = DownloadProgressState()
    }
}
