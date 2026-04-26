package com.creamaker.changli_planet_app.feature.common.compose_ui

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.widget.view.CustomToast

@Immutable
data class FunctionShortcut(
    val id: String,
    val title: String,
    @DrawableRes val iconRes: Int,
    val tintColor: Color,
    val destination: FunctionDestination
)

enum class FunctionDestination {
    Timetable,
    ScoreInquiry,
    CampusMap,
    Classroom,
    Homework,
    Electronic,
    ExamArrangement,
    Calendar,
    Cet,
    LostFound,
    AccountBook,
    Contract,
    Mandarin
}

fun primaryFunctionShortcuts(): List<FunctionShortcut> =
    listOf(
        FunctionShortcut("schedule", "课表", R.drawable.ic_timetable, FunctionColors.Schedule, FunctionDestination.Timetable),
        FunctionShortcut("grade", "成绩查询", R.drawable.ic_exam, FunctionColors.Grade, FunctionDestination.ScoreInquiry),
        FunctionShortcut("map", "校园地图", R.drawable.ic_map, FunctionColors.Map, FunctionDestination.CampusMap),
        FunctionShortcut("classroom", "空教室", R.drawable.ic_classroom, FunctionColors.Classroom, FunctionDestination.Classroom),
        FunctionShortcut("homework", "作业查询", R.drawable.ic_homework, FunctionColors.Homework, FunctionDestination.Homework),
        FunctionShortcut("electric", "电费查询", R.drawable.ic_bill, FunctionColors.Electric, FunctionDestination.Electronic),
        FunctionShortcut("exam", "考试安排", R.drawable.ic_schedule, FunctionColors.Exam, FunctionDestination.ExamArrangement),
        FunctionShortcut("calendar", "校历", R.drawable.ic_calendar, FunctionColors.Calendar, FunctionDestination.Calendar),
    )

fun secondaryFunctionShortcuts(): List<FunctionShortcut> =
    listOf(
        FunctionShortcut("cet", "四六级", R.drawable.ic_essay, FunctionColors.CET, FunctionDestination.Cet),
        FunctionShortcut("lost_found", "失物招领", R.drawable.ic_lost_and_found, FunctionColors.LostFound, FunctionDestination.LostFound),
        FunctionShortcut("account", "记账本", R.drawable.ic_account_book, FunctionColors.Account, FunctionDestination.AccountBook),
        FunctionShortcut("document", "资料库", R.drawable.ic_document, FunctionColors.Document, FunctionDestination.Contract),
        FunctionShortcut("mandarin", "普通话", R.drawable.ic_talking, FunctionColors.Mandarin, FunctionDestination.Mandarin),
    )

fun FunctionShortcut.toFunctionItemData(context: Context): FunctionItemData =
    FunctionItemData(
        id = id,
        title = title,
        iconRes = iconRes,
        tintColor = tintColor,
        onClick = { openFunctionShortcut(context, destination) }
    )

fun openFunctionShortcut(context: Context, destination: FunctionDestination) {
    when (destination) {
        FunctionDestination.Timetable -> Route.goTimetable(context)
        FunctionDestination.ScoreInquiry -> Route.goScoreInquiry(context)
        FunctionDestination.CampusMap -> Route.goCampusMap(context)
        FunctionDestination.Classroom -> Route.goClassInfo(context)
        FunctionDestination.Homework -> Route.goMooc(context)
        FunctionDestination.Electronic -> Route.goElectronic(context)
        FunctionDestination.ExamArrangement -> Route.goExamArrangement(context)
        FunctionDestination.Calendar -> Route.goCalendar(context)
        FunctionDestination.Cet -> Route.goCet(context)
        FunctionDestination.LostFound -> CustomToast.showMessage(context, "正在全力开发中")
        FunctionDestination.AccountBook -> Route.goAccountBook(context)
        FunctionDestination.Contract -> CustomToast.showMessage(context, "正在全力开发中")
        FunctionDestination.Mandarin -> Route.goMande(context)
    }
}
