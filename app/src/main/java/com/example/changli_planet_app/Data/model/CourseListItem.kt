package com.example.changli_planet_app.Data.model

sealed class CourseListItem {
    data class SemesterItem(
        val semester: SemesterGroup,
        val isExpanded: Boolean = true
    ) : CourseListItem()
}