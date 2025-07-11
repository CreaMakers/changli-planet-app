package com.example.changli_planet_app

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.example.changli_planet_app.Activity.Store.TimeTableStore.weekJsonInfo
import com.example.changli_planet_app.Cache.Room.entity.MySubject
import com.example.changli_planet_app.Cache.StudentInfoManager
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Data.CommonInfo
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.Course
import com.example.changli_planet_app.Network.Response.MyResponse
import com.google.gson.reflect.TypeToken
import okhttp3.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Implementation of App Widget functionality.
 */
class TimeTableAppWidget : AppWidgetProvider() {

    private val TAG = "TimeTableAppWidget"
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
private val curTerm by lazy { getCurrentTerm() }
private val curWeek by lazy { getCurrentWeek() }

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    Log.d("TimeTableAppWidget", "updateAppWidget")
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.time_table_app_widget)
    views.setTextViewText(R.id.term_tv, curTerm)
    views.setTextViewText(R.id.date_tv, getCurrentMonthAndDay())
    views.setTextViewText(R.id.week_tv, when(curWeek) {
        1 -> "星期一"
        2 -> "星期二"
        3 -> "星期三"
        4 -> "星期四"
        5 -> "星期五"
        6 -> "星期六"
        else -> "星期天"
    })
    if (isHolidayOrError()) {
        views.setViewVisibility(R.id.end_tv, View.VISIBLE)
        views.setTextViewText(R.id.end_tv, "还没有开学哦٩(◦`꒳´◦)۶")
    } else {

    }
    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}


private fun getCourseInfo() {
    val subjects = mutableListOf<MySubject>()
    //获得网络请求
    val httpUrlHelper = HttpUrlHelper.HttpRequest()
        .get(PlanetApplication.ToolIp + "/courses")
        .addQueryParam("stuNum", studentId)
        .addQueryParam("password", studentPassword)
        .addQueryParam("week", curWeek.toString())
        .addQueryParam("termId", curTerm).build()

    OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
        override fun onSuccess(response: Response) {
            try {
                val type = object : TypeToken<MyResponse<List<Course>>>() {}.type
                val fromJson = OkHttpHelper.gson.fromJson<MyResponse<List<Course>>>(
                    response.body?.string(), type
                )

                when (fromJson.code) {
                    "200" -> {
                        subjects.addAll(generateSubjects(fromJson.data, curTerm))
                    }

                    else -> {
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onFailure(error: String) {
        }
    })
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

private fun isHolidayOrError(): Boolean {
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

private fun generateSubjects(courses: List<Course>, newTerm: String): MutableList<MySubject> {
    val subjects = mutableListOf<MySubject>()
    courses.forEach {
        val weeks = parseWeeks(it.weeks).weeks
        val start = parseWeeks(it.weeks).start
        val step = parseWeeks(it.weeks).step
        subjects.add(
            MySubject(
                courseName = it.courseName,
                classroom = it.classroom,
                teacher = it.teacher,
                weeks = weeks,
                start = start,
                step = step,
                weekday = it.weekday.toInt(),
                term = newTerm,
                studentId = studentId,
                studentPassword = studentPassword
            )
        )
    }
    return subjects
}

private fun parseWeeks(weekJson: String): weekJsonInfo {
    // 匹配周数范围、单双周（可选）、以及节次范围
    val pattern = java.util.regex.Pattern.compile(
        "(\\d+(?:-\\d+)?(?:,\\d+(?:-\\d+)?)*)\\((周|单周|双周)?\\)?\\[(\\d{2})(?:-(\\d{2}))?(?:-(\\d{2}))?(?:-(\\d{2}))?节\\]"
    )
    val matcher = pattern.matcher(weekJson)
    if (matcher.find()) {
        val weeksRange = matcher.group(1) // 周数范围
        val weekType = matcher.group(2)   // 单周、双周、周
        val startClass = matcher.group(3)?.toIntOrNull() ?: 0 // 起始节次
        val endClass = listOfNotNull(
            matcher.group(4)?.toIntOrNull(),
            matcher.group(5)?.toIntOrNull(),
            matcher.group(6)?.toIntOrNull(),
        ).lastOrNull() ?: startClass // 结束节次

        val step = endClass - startClass + 1 // 计算 step（持续节次数量）

        // 解析周数列表
        val weeks = try {
            weeksRange?.let { range ->
                range.split(",").flatMap { part ->
                    if (part.contains("-")) {
                        // 范围周数处理，例如 "1-3"
                        val (weekStart, weekEnd) = part.split("-").map { it.toInt() }
                        when (weekType) {
                            "单周" -> (weekStart..weekEnd).filter { it % 2 != 0 } // 奇数周
                            "双周" -> (weekStart..weekEnd).filter { it % 2 == 0 } // 偶数周
                            else -> (weekStart..weekEnd).toList() // 全部周（包括 null 和 "周"）
                        }
                    } else {
                        // 单个周数处理，例如 "5"
                        listOf(part.toInt())
                    }
                }
            } ?: listOf()
        } catch (e: Exception) {
            listOf() // 异常时返回空列表
        }

        // 返回解析结果
        return weekJsonInfo(weeks, startClass, step)
    }

    // 返回空的结果
    return weekJsonInfo(listOf(), 0, 0)
}

