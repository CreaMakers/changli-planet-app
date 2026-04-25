package com.creamaker.changli_planet_app.feature.calendar.ui.model

import androidx.compose.runtime.Immutable
import com.creamaker.changli_planet_app.feature.calendar.data.remote.dto.SemesterCalendarDetail
import com.creamaker.changli_planet_app.feature.calendar.data.remote.dto.SemesterCalendarListItem

@Immutable
data class SemesterCalendarListUiState(
    val isLoading: Boolean = false,
    val items: List<SemesterCalendarListItem> = emptyList(),
    val errorMessage: String = ""
)

@Immutable
data class SemesterCalendarDetailUiState(
    val isLoading: Boolean = false,
    val detail: SemesterCalendarDetail? = null,
    val errorMessage: String = ""
)
