package com.creamaker.changli_planet_app.overview.ui.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class OverviewUiState(
    val isRefreshing: Boolean = false,
    val isSilentSyncing: Boolean = false,
    val isBoundStudent: Boolean = false,
    val isOnline: Boolean = false,
    val isElectricityBound: Boolean = false,
    val accountName: String = "长理星球",
    val avatarUrl: String = "",
    val studentId: String = "",
    val dateText: String = "",
    val currentTerm: String = "",
    val currentWeek: Int = 1,
    val dataSourceLabel: String = "正在整理本地数据",
    val metrics: List<OverviewMetricUiModel> = emptyList(),
    val todayCourses: List<OverviewCourseUiModel> = emptyList(),
    val todayCourseMessage: String = "",
    val isShowingTomorrow: Boolean = false,
    val pendingHomeworks: List<OverviewHomeworkUiModel> = emptyList(),
    val pendingHomeworkMessage: String = "",
    val pendingTests: List<OverviewTestUiModel> = emptyList(),
    val pendingTestMessage: String = "",
    val upcomingExams: List<OverviewExamUiModel> = emptyList(),
    val examMessage: String = ""
)

@Immutable
data class OverviewMetricUiModel(
    val id: String,
    val title: String,
    val value: String,
    val unit: String = "",
    val subtitle: String,
    val secondarySubtitle: String = "",
    @DrawableRes val iconRes: Int,
    val accentColor: Color
)

@Immutable
data class OverviewCourseUiModel(
    val id: String,
    val courseName: String,
    val classroom: String,
    val teacher: String,
    val timeText: String,
    val accentLabel: String,
    val accentColor: Color
)

@Immutable
data class OverviewExamUiModel(
    val id: String,
    val courseName: String,
    val examTime: String,
    val location: String,
    val badge: String = "待安排"
)

@Immutable
data class OverviewHomeworkUiModel(
    val id: String,
    val title: String,
    val courseName: String = "",
    val deadlineText: String = "",
    val urgencyText: String = "",
    val isUrgent: Boolean = false,
    val statusText: String = "待提交"
)

@Immutable
data class OverviewTestUiModel(
    val id: String,
    val title: String,
    val courseName: String = "",
    val timeText: String = "",
    val urgencyText: String = "",
    val isUrgent: Boolean = false,
    val statusText: String = "待测试"
)
