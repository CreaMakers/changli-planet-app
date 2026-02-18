package com.creamaker.changli_planet_app.feature.common.ui.adapter.model

import androidx.annotation.Keep

@Keep
sealed class CourseListItem {
    data class SemesterItem(
        val semester: SemesterGroup,
        val isExpanded: Boolean = true
    ) : CourseListItem()
}