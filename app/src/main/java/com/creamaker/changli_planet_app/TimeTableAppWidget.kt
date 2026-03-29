package com.creamaker.changli_planet_app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.creamaker.changli_planet_app.common.cache.CommonInfo
import com.creamaker.changli_planet_app.common.data.local.mmkv.StudentInfoManager
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.feature.common.data.local.entity.TimeTableMySubject
import com.creamaker.changli_planet_app.feature.common.data.local.room.database.CoursesDataBase
import com.creamaker.changli_planet_app.feature.timetable.ui.TimeTableActivity
import com.creamaker.changli_planet_app.utils.ResourceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Implementation of App Widget functionality.
 */
class TimeTableAppWidget : AppWidgetProvider() {

    companion object {
        private const val TAG = "TimeTableAppWidget"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            Log.d(TAG, "onUpdate")
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        Log.d(TAG, "onEnabled")
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        Log.d(TAG, "onDisabled")
        // Enter relevant functionality for when the last widget is disabled
    }
}

private val studentId by lazy { StudentInfoManager.studentId }
private val studentPassword by lazy { StudentInfoManager.studentPassword }

private val times = arrayOf(
    "8:00\n8:45", "8:55\n9:40", "10:10\n10:55", "11:05\n11:50",
    "14:00\n14:45", "14:55\n15:40", "16:10\n16:55", "17:05\n17:50",
    "19:30\n20:15", "20:25\n21:10"
)

private const val TAG = "TimeTableAppWidget"

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    Log.d("TimeTableAppWidget", "updateAppWidget")
    val curTerm = getCurrentTerm()
    val curWeekDay = getCurrentWeek()
    val curSchoolWeek = getCurrentSchoolWeek(curTerm)
    // 创建点击意图，拉起主Activity
    val intent = Intent(context, TimeTableActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra("from_timeTable_widget",true)
    }


    val pendingIntent = PendingIntent.getActivity(
        context,
        appWidgetId, // 使用 appWidgetId 作为 requestCode 确保唯一性
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.time_table_app_widget)
    // 为整个小组件设置点击事件
    views.setOnClickPendingIntent(R.id.widget_root_layout, pendingIntent)
    val currentMonthAndDay = getCurrentMonthAndDay()
    val weekTextAuto = CommonInfo.getCurrentWeekAuto()
    views.setTextViewText(R.id.term_tv, weekTextAuto)
    views.setTextViewText(R.id.date_tv, currentMonthAndDay)
    views.setTextViewText(
        R.id.week_tv, when (curWeekDay) {
            1 -> "周一"
            2 -> "周二"
            3 -> "周三"
            4 -> "周四"
            5 -> "周五"
            6 -> "周六"
            else -> "周日"
        }
    )
    Log.d(TAG, "curTerm: $curTerm currentMonthAndDay: $currentMonthAndDay curWeekDay: $curWeekDay")
    if (studentId.isEmpty() or studentPassword.isEmpty()) {
        views.setViewVisibility(R.id.student_error_tv, View.VISIBLE)
        appWidgetManager.updateAppWidget(appWidgetId, views)
        return
    }
    views.setViewVisibility(R.id.student_error_tv, View.GONE)
    if (isHolidayOrError(curTerm)) {
        views.setViewVisibility(R.id.end_tv, View.VISIBLE)
        views.setTextViewText(R.id.end_tv, "还没有开学哦٩(◦`꒳´◦)۶")
        appWidgetManager.updateAppWidget(appWidgetId, views)
    } else {
        getCourseInfo { courses ->
            if (courses.isNullOrEmpty()) {
                if (courses == null) {
                    views.setViewVisibility(R.id.student_error_tv, View.VISIBLE)
                } else {
                    views.setViewVisibility(R.id.student_error_tv, View.GONE)
                    views.setViewVisibility(R.id.no_course_tv, View.VISIBLE)
                }
            } else {
                views.setViewVisibility(R.id.student_error_tv, View.GONE)
                val result = courses.asSequence().filter { it.weekday == curWeekDay }
                    .filter { !it.weeks.isNullOrEmpty() and it.weeks!!.contains(curSchoolWeek) }
                    .sortedBy { it.start }
                    .filter { course ->
                        val now = Calendar.getInstance()
                        val currentMinutes =
                            now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

                        if (course.start > 0 && course.start <= times.size) {
                            val timeStr = times[course.start - 1].split("\n")[0]
                            val timeParts = timeStr.split(":")
                            val courseMinutes =
                                timeParts[0].toInt() * 60 + timeParts[1].toInt()
                            courseMinutes > currentMinutes
                        } else {
                            false
                        }
                    }
                    .take(2).toList()
                Log.d("TimeTableAppWidget", "result size: ${result.size}")
                if (result.isEmpty()) {
                    Log.d("TimeTableAppWidget", "no_classes_today")
                    views.setViewVisibility(R.id.no_course_tv, View.VISIBLE)
                    views.setViewVisibility(R.id.course1_ll, View.GONE)
                    views.setViewVisibility(R.id.course2_ll, View.GONE)
                } else {
                    views.setViewVisibility(R.id.no_course_tv, View.GONE)
                    val course1 = result.getOrNull(0)
                    val course2 = result.getOrNull(1)
                    course1?.let {
                        views.setViewVisibility(R.id.course1_ll, View.VISIBLE)
                        views.setTextViewText(R.id.tv_course_name_1, it.courseName)
                        views.setTextViewText(
                            R.id.tv_course_room_1,
                            if (!it.classroom.isNullOrEmpty()) it.classroom else ResourceUtil.getStringRes(
                                R.string.no_classroom
                            )
                        )
                        views.setTextViewText(
                            R.id.tv_course_time_1,
                            "${times[it.start - 1].split("\n")[0]} - ${
                                times[it.start + it.step - 2].split(
                                    "\n"
                                )[1]
                            }"
                        )
                    }
                    views.setViewVisibility(R.id.course2_ll, View.GONE)
                    course2?.let {
                        views.setViewVisibility(R.id.course2_ll, View.VISIBLE)
                        views.setTextViewText(R.id.tv_course_name_2, it.courseName)
                        views.setTextViewText(
                            R.id.tv_course_room_2,
                            if (!it.classroom.isNullOrEmpty()) it.classroom else ResourceUtil.getStringRes(
                                R.string.no_classroom
                            )
                        )
                        views.setTextViewText(
                            R.id.tv_course_time_2,
                            "${times[it.start - 1].split("\n")[0]} - ${
                                times[it.start + it.step - 2].split(
                                    "\n"
                                )[1]
                            }"
                        )
                    }
                }
            }
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

private fun getCourseInfo(callback: (List<TimeTableMySubject>?) -> Unit) {
    val curTerm = getCurrentTerm()
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val courses = CoursesDataBase.getDatabase(PlanetApplication.appContext)
                .courseDao()
                .getCoursesByTerm(curTerm, studentId, studentPassword)
                .distinctBy {
                    "${it.courseName}${it.teacher}${it.weeks}${it.classroom}${it.start}${it.step}${it.term}${it.weekday}"
                }
                .map { course ->
                    if (course.weekday == 7) {
                        val adjustedWeeks = course.weeks?.map { week -> week - 1 }
                        course.copy(weeks = adjustedWeeks)
                    } else {
                        course
                    }
                }
            withContext(Dispatchers.Main) {
                callback(courses)
            }
        } catch (e: Exception) {
            Log.e(TAG, "读取课表数据库失败", e)
            withContext(Dispatchers.Main) {
                callback(null)
            }
        }
    }
}

private fun getCurrentTerm(): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"))
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH) + 1
    return when {
        currentMonth >= 7 -> "$currentYear-${currentYear + 1}-1"  // 第一学期
        currentMonth >= 2 -> "${currentYear - 1}-${currentYear}-2"  // 第二学期
        else -> "${currentYear - 1}-${currentYear}-1"  // 上学年第一学期
    }
}

private fun getCurrentSchoolWeek(curTerm: String): Int {
    val startTime = CommonInfo.termMap[curTerm]!!
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val startDate = formatter.parse(startTime)!!
    val currentDate = Date()
    val diffTime = currentDate.time - startDate.time
    return ((diffTime / 1000 / 3600 / 24) / 7 + 1).toInt()
}

private fun isHolidayOrError(curTerm: String): Boolean {
    val startTime = CommonInfo.termMap[curTerm] ?: return true
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val startDate = formatter.parse(startTime)
    val currentDate = Date()
    return !(startDate != null && currentDate >= startDate)
}

private fun getCurrentMonthAndDay(): String {
    val formatter = SimpleDateFormat("M.d", Locale.getDefault())
    return formatter.format(Date())
}

private fun getCurrentWeek(): Int {
    return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        Calendar.SUNDAY -> 7
        else -> -1
    }
}
