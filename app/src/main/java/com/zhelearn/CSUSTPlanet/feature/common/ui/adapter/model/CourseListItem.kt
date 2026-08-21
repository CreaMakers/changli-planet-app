package com.zhelearn.CSUSTPlanet.feature.common.ui.adapter.model

import androidx.annotation.Keep

@Keep
sealed class CourseListItem {
    data class SemesterItem(
        val semester: SemesterGroup,
        val isExpanded: Boolean = true
    ) : CourseListItem()
}