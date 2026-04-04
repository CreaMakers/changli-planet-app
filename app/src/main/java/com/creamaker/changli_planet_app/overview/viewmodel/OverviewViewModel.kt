package com.creamaker.changli_planet_app.overview.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.overview.data.OverviewRepository
import com.creamaker.changli_planet_app.overview.ui.model.OverviewMetricUiModel
import com.creamaker.changli_planet_app.overview.ui.model.OverviewUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OverviewViewModel : ViewModel() {
    private val repository by lazy { OverviewRepository(PlanetApplication.appContext) }
    private var loadJob: Job? = null

    private val _uiState = MutableStateFlow(OverviewUiState())
    val uiState: StateFlow<OverviewUiState> = _uiState.asStateFlow()

    init {
        load(preserveVisibleContent = false)
    }

    fun onResume() {
        load(preserveVisibleContent = true)
    }

    private fun load(preserveVisibleContent: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val currentState = _uiState.value
            val localState = repository.loadLocalState()
            val displayState = mergeForDisplay(
                current = currentState,
                incoming = localState,
                preserveVisibleContent = preserveVisibleContent
            ).copy(
                isSilentSyncing = localState.isBoundStudent && localState.isOnline
            )
            _uiState.value = displayState
            if (localState.isBoundStudent && localState.isOnline) {
                val refreshedState = repository.refreshState()
                _uiState.value = mergeForDisplay(
                    current = displayState,
                    incoming = refreshedState,
                    preserveVisibleContent = true
                )
            }
        }
    }

    private fun mergeForDisplay(
        current: OverviewUiState,
        incoming: OverviewUiState,
        preserveVisibleContent: Boolean
    ): OverviewUiState {
        if (!preserveVisibleContent) return incoming

        val keepCourses = current.todayCourses.isNotEmpty() && incoming.todayCourses.isEmpty()
        val keepHomeworks = current.pendingHomeworks.isNotEmpty() && incoming.pendingHomeworks.isEmpty()
        val keepTests = current.pendingTests.isNotEmpty() && incoming.pendingTests.isEmpty()
        val keepExams = current.upcomingExams.isNotEmpty() && incoming.upcomingExams.isEmpty()

        return incoming.copy(
            metrics = mergeMetrics(current.metrics, incoming.metrics),
            todayCourses = if (keepCourses) current.todayCourses else incoming.todayCourses,
            todayCourseMessage = if (keepCourses) current.todayCourseMessage else incoming.todayCourseMessage,
            pendingHomeworks = if (keepHomeworks) current.pendingHomeworks else incoming.pendingHomeworks,
            pendingHomeworkMessage = if (keepHomeworks) current.pendingHomeworkMessage else incoming.pendingHomeworkMessage,
            pendingTests = if (keepTests) current.pendingTests else incoming.pendingTests,
            pendingTestMessage = if (keepTests) current.pendingTestMessage else incoming.pendingTestMessage,
            upcomingExams = if (keepExams) current.upcomingExams else incoming.upcomingExams,
            examMessage = if (keepExams) current.examMessage else incoming.examMessage
        )
    }

    private fun mergeMetrics(
        current: List<OverviewMetricUiModel>,
        incoming: List<OverviewMetricUiModel>
    ): List<OverviewMetricUiModel> {
        if (current.isEmpty()) return incoming
        if (incoming.isEmpty()) return current

        val currentById = current.associateBy { it.id }
        return incoming.map { metric ->
            val previous = currentById[metric.id]
            if (previous != null && previous.hasVisibleValue() && !metric.hasVisibleValue()) previous else metric
        }
    }

    private fun OverviewMetricUiModel.hasVisibleValue(): Boolean =
        value.isNotBlank() && value != "--"
}
