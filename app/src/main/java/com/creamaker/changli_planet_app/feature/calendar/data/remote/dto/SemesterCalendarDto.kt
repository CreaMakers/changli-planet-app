package com.creamaker.changli_planet_app.feature.calendar.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * 校历列表项，对应 `GET /v1/config/semester-calendars` 返回数组的单个元素。
 *
 * @property semesterCode 学期代码，如 `"2025-2026-2"`
 * @property title 学年标题，如 `"2025-2026学年度校历"`
 * @property subtitle 学期副标题，如 `"第二学期"`
 */
@Keep
data class SemesterCalendarListItem(
    @SerializedName("semesterCode") val semesterCode: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("subtitle") val subtitle: String = ""
)

/**
 * 校历详情，对应 `GET /v1/config/semester-calendars/{semester_code}` 返回体。
 *
 * 所有时间字段均为 ISO 8601 UTC 字符串（例如 `"2026-03-09T00:00:00Z"`），
 * 由 [com.creamaker.changli_planet_app.feature.calendar.ui.compose.parseIsoDateMs] 解析为本地 00:00 毫秒。
 *
 * @property semesterCode 学期代码
 * @property title 学年标题
 * @property subtitle 学期副标题
 * @property calendarStart 校历显示起始日（比学期正式开始早几周，用于显示假期/准备周）
 * @property calendarEnd 校历显示结束日
 * @property semesterStart 学期正式开始日（课表"第 1 周周一"定位锚点）
 * @property semesterEnd 学期正式结束日
 * @property notes 按周挂载的文字备注列表
 * @property customWeekRanges 自定义周次范围（如"假期"、"准备周"），展示为整行底色标签
 */
@Keep
data class SemesterCalendarDetail(
    @SerializedName("semesterCode") val semesterCode: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("subtitle") val subtitle: String = "",
    @SerializedName("calendarStart") val calendarStart: String = "",
    @SerializedName("calendarEnd") val calendarEnd: String = "",
    @SerializedName("semesterStart") val semesterStart: String = "",
    @SerializedName("semesterEnd") val semesterEnd: String = "",
    @SerializedName("notes") val notes: List<SemesterCalendarNote> = emptyList(),
    @SerializedName("customWeekRanges") val customWeekRanges: List<SemesterCalendarWeekRange> = emptyList()
)

/**
 * 挂在特定教学周上的文字说明。
 *
 * @property row 所在周次（1-based）
 * @property content 备注文本，如 `"3月9日正式上课"`
 * @property needNumber `true` 时表示该备注需要在周次标号旁强调显示（在客户端表现为周次角标红点 + 正文加粗）
 */
@Keep
data class SemesterCalendarNote(
    @SerializedName("row") val row: Int = 0,
    @SerializedName("content") val content: String = "",
    @SerializedName("needNumber") val needNumber: Boolean = false
)

/**
 * 自定义周次范围，用于展示"假期"、"准备周"等整行色块标签。
 *
 * 客户端对重叠范围采用"保留首个"策略，重叠时第二个不会覆盖第一个。
 *
 * @property startRow 起始周次（1-based，含）
 * @property endRow 结束周次（1-based，含）
 * @property content 标签文字，如 `"假期"`
 */
@Keep
data class SemesterCalendarWeekRange(
    @SerializedName("startRow") val startRow: Int = 0,
    @SerializedName("endRow") val endRow: Int = 0,
    @SerializedName("content") val content: String = ""
)
