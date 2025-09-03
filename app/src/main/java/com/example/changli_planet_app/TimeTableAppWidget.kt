package com.example.changli_planet_app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.example.changli_planet_app.Cache.Room.entity.MySubject
import com.example.changli_planet_app.Cache.StudentInfoManager
import com.example.changli_planet_app.Core.MainActivity
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Data.CommonInfo
import com.example.changli_planet_app.Feature.common.store.TimeTableStore.weekJsonInfo
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.Course
import com.example.changli_planet_app.Network.Response.MyResponse
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV
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
private val curWeekDay by lazy { getCurrentWeek() }
private val curSchoolWeek: Int by lazy { getCurrentSchoolWeek() }

private val mmkv by lazy { MMKV.mmkvWithID(TAG) }
var isRefresh: Boolean
    get() = mmkv.getBoolean("isRefresh", true)
    set(value) {
        mmkv.putBoolean("isRefresh", value)
    }

private var courses: String
    get() = mmkv.getString("courses", "") ?: ""
    set(value) {
        mmkv.putString("courses", value)
    }

private val times = arrayOf(
    "8:00\n8:45", "8:55\n9:40", "10:10\n10:55", "11:05\n11:50",
    "14:00\n14:45", "14:55\n15:40", "16:10\n16:55", "17:05\n17:50",
    "19:30\n20:15", "20:25\n21:10"
)

private const val TAG = "TimeTableAppWidget"

private const val refreshCount = 3
internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    Log.d("TimeTableAppWidget", "updateAppWidget")
    // 创建点击意图，拉起主Activity
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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
    views.setTextViewText(R.id.term_tv, curTerm)
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
    if (isHolidayOrError()) {
        views.setViewVisibility(R.id.end_tv, View.VISIBLE)
        views.setTextViewText(R.id.end_tv, "还没有开学哦٩(◦`꒳´◦)۶")
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    } else {
        getCourseInfo { courses ->
            if (courses.isNullOrEmpty()) {
                views.setViewVisibility(R.id.no_course_tv, View.VISIBLE)
            } else {
                val result = courses.filter { it.weekday == curWeekDay }
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
                    .take(2)
                Log.d("TimeTableAppWidget","result size: ${result.size}")
                if (result.isEmpty()) {
                    Log.d("TimeTableAppWidget","no_classes_today")
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
                        views.setTextViewText(R.id.tv_course_room_1, it.classroom)
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
                        views.setTextViewText(R.id.tv_course_room_2, it.classroom)
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

private fun getCourseInfo(callback: (List<MySubject>?) -> Unit) {
    val subjects = mutableListOf<MySubject>()
    if (!isRefresh) {
        callback(OkHttpHelper.gson.fromJson(courses, object : TypeToken<List<MySubject>>() {}.type))
        return
    }
    val httpUrlHelper = HttpUrlHelper.HttpRequest()
        .get(PlanetApplication.ToolIp + "/courses")
        .addQueryParam("stuNum", studentId)
        .addQueryParam("password", studentPassword)
        .addQueryParam("week", "")
        .addQueryParam("termId", curTerm).build()
    var isSuccess = false
    for (i in 0 until refreshCount) {
        OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
            override fun onSuccess(response: Response) {
                try {
                    val type = object : TypeToken<MyResponse<List<Course>>>() {}.type
                    val fromJson = OkHttpHelper.gson.fromJson<MyResponse<List<Course>>>(
                        response.body?.string(), type
                    )

                    when (fromJson.code) {
                        "200" -> {
                            Log.d("TimeTableAppWidget", "获取课表成功，刷新缓存")
                            subjects.addAll(generateSubjects(fromJson.data, curTerm))
                            val result = subjects.distinctBy {
                                "${it.courseName}${it.teacher}${it.weeks}${it.classroom}${it.start}${it.step}${it.term}"
                            }
                            courses = OkHttpHelper.gson.toJson(result.toList())
                            isRefresh = false
                            callback(result) // 在这里调用回调
                            isSuccess = true
                            return
                        }

                        else -> {
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(error: String) {
                Log.e("TimeTableAppWidget", "getCourseInfo error: $error")
            }
        })
        if (isSuccess) return
        callback(OkHttpHelper.gson.fromJson(courses, object : TypeToken<List<MySubject>>() {}.type))
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

private fun getCurrentSchoolWeek(): Int {
    val startTime = CommonInfo.termMap[curTerm]!!
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val startDate = formatter.parse(startTime)!!
    val currentDate = Date()
    val diffTime = currentDate.time - startDate.time
    return ((diffTime / 1000 / 3600 / 24) / 7 + 1).toInt()
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
            e.printStackTrace()
            listOf() // 异常时返回空列表
        }

        // 返回解析结果
        return weekJsonInfo(weeks, startClass, step)
    }

    // 返回空的结果
    return weekJsonInfo(listOf(), 0, 0)
}

