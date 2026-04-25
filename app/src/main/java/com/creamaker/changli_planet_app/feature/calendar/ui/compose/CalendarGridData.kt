package com.creamaker.changli_planet_app.feature.calendar.ui.compose

import com.creamaker.changli_planet_app.feature.calendar.data.remote.dto.SemesterCalendarDetail
import com.creamaker.changli_planet_app.feature.calendar.data.remote.dto.SemesterCalendarNote
import com.creamaker.changli_planet_app.feature.calendar.data.remote.dto.SemesterCalendarWeekRange
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ---------------- 常量 ----------------

internal const val CALENDAR_DATE_PATTERN = "yyyy-MM-dd"
internal const val CALENDAR_DISPLAY_DATE_PATTERN = "yyyy年M月d日"
internal const val MS_PER_DAY: Long = 24L * 60 * 60 * 1000

// ---------------- 数据模型 ----------------

/**
 * 单日数据。
 *
 * @property dateMs 该日 00:00:00（系统默认时区）毫秒时间戳
 * @property dayOfMonth 日（1..31）
 * @property month 月（1..12）
 */
internal data class DayCellData(
    val dateMs: Long,
    val dayOfMonth: Int,
    val month: Int
)

/**
 * 每周一行数据。
 *
 * 周的定义：**周日为起始（美式周），周六为结束**。与服务端 `customWeekRanges.startRow` /
 * `notes.row` 的计数方式一致。国内教务系统使用这种定义：一学期第 1 周 = `calendarStart`
 * 所在的"周日到周六"。
 *
 * @property row 校历行号（1-based，与服务端 `notes.row` / `customWeekRanges.startRow` 对齐）。
 *               **不等于** UI 上显示的"教学周序号"。
 * @property days 7 天，`days[0]=周日 .. days[6]=周六`
 * @property startMs 本周周日 00:00 毫秒时间戳
 * @property endMs 本周周六 00:00 毫秒时间戳
 */
internal data class WeekRow(
    val row: Int,
    val days: List<DayCellData?>,
    val startMs: Long?,
    val endMs: Long?
)

// ---------------- 工具函数 ----------------

/**
 * 根据 [SemesterCalendarDetail.calendarStart] / [SemesterCalendarDetail.calendarEnd] 按周组装行数据。
 *
 * 约定：以 `calendarStart` 所在**周日**作为第 1 周的起始（与服务端 row 计数一致），
 * 一直铺到 `calendarEnd` 所在周的周六。
 *
 * @return 按周次升序排列的 [WeekRow] 列表；若起止解析失败或范围无效则返回空列表
 */
internal fun buildWeekRows(detail: SemesterCalendarDetail): List<WeekRow> {
    val calStartMs = parseIsoDateMs(detail.calendarStart) ?: return emptyList()
    val calEndMs = parseIsoDateMs(detail.calendarEnd) ?: return emptyList()
    if (calEndMs < calStartMs) return emptyList()

    // 回退到 calendarStart 所在的"周日"，作为第 1 周的起点
    val firstSundayCal = Calendar.getInstance().apply {
        timeInMillis = calStartMs
        clearTime()
        // Calendar.SUNDAY = 1, MONDAY = 2, ..., SATURDAY = 7
        // 到本周周日要倒退 (dow - 1) 天
        val dow = get(Calendar.DAY_OF_WEEK)
        add(Calendar.DAY_OF_MONTH, -(dow - Calendar.SUNDAY))
    }

    val totalDays = ((calEndMs - firstSundayCal.timeInMillis) / MS_PER_DAY).toInt() + 1
    val totalWeeks = ((totalDays + 6) / 7).coerceAtLeast(0)
    if (totalWeeks == 0) return emptyList()

    val rows = ArrayList<WeekRow>(totalWeeks)
    val cursor = Calendar.getInstance().apply { timeInMillis = firstSundayCal.timeInMillis }
    for (weekIndex in 1..totalWeeks) {
        val days = ArrayList<DayCellData?>(7)
        var weekStartMs: Long? = null
        var weekEndMs: Long? = null
        // days[0] = 周日, days[1] = 周一, ..., days[6] = 周六
        for (d in 0..6) {
            val ms = cursor.timeInMillis
            if (d == 0) weekStartMs = ms
            if (d == 6) weekEndMs = ms
            days.add(
                DayCellData(
                    dateMs = ms,
                    dayOfMonth = cursor.get(Calendar.DAY_OF_MONTH),
                    month = cursor.get(Calendar.MONTH) + 1
                )
            )
            cursor.add(Calendar.DAY_OF_MONTH, 1)
        }
        rows.add(WeekRow(row = weekIndex, days = days, startMs = weekStartMs, endMs = weekEndMs))
    }
    return rows
}

/**
 * 将 ISO 8601 时间字符串（如 `2026-03-09T00:00:00Z` 或 `2026-03-09`）
 * 解析为**本地时区 00:00:00** 的毫秒时间戳。
 *
 * 只取日期部分（T 前 10 个字符），按默认时区 0 点为准，避免 UTC 偏移导致跨日。
 */
internal fun parseIsoDateMs(iso: String): Long? {
    if (iso.isBlank()) return null
    val datePart = iso.substringBefore('T').take(10)
    if (datePart.length != 10) return null
    return runCatching {
        val fmt = SimpleDateFormat(CALENDAR_DATE_PATTERN, Locale.CHINA).apply {
            timeZone = TimeZone.getDefault()
        }
        val parsed = fmt.parse(datePart) ?: return null
        Calendar.getInstance().apply {
            time = parsed
            clearTime()
        }.timeInMillis
    }.getOrNull()
}

/** 今日 00:00 毫秒时间戳。 */
internal fun todayAtMidnightMs(): Long =
    Calendar.getInstance().apply { clearTime() }.timeInMillis

internal fun Calendar.clearTime() {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

/** ISO 字符串 → `yyyy年M月d日`；失败返回 `"--"`。 */
internal fun formatDisplayDate(iso: String): String {
    val ms = parseIsoDateMs(iso) ?: return "--"
    return SimpleDateFormat(CALENDAR_DISPLAY_DATE_PATTERN, Locale.CHINA).format(Date(ms))
}

/**
 * 将 [SemesterCalendarDetail.customWeekRanges] 展平为 `row → range` 的映射。
 *
 * 重叠策略：**保留首个**（后者不覆盖前者），避免服务端误配时"假期周"被"准备周"吞掉。
 */
internal fun flattenRanges(ranges: List<SemesterCalendarWeekRange>): Map<Int, SemesterCalendarWeekRange> {
    if (ranges.isEmpty()) return emptyMap()
    val map = HashMap<Int, SemesterCalendarWeekRange>()
    ranges.forEach { range ->
        for (row in range.startRow..range.endRow) {
            // putIfAbsent：首个生效；若后续 range 想覆盖，服务端应显式去重
            if (!map.containsKey(row)) map[row] = range
        }
    }
    return map
}

/**
 * 带全局索引的备注（用于在视图侧稳定区分"同周同文"的两条 needNumber 备注）。
 *
 * @property globalIndex 该备注在 [SemesterCalendarDetail.notes] 原列表中的下标（0-based）
 * @property note 原始备注
 */
internal data class IndexedNote(val globalIndex: Int, val note: SemesterCalendarNote)

/**
 * 根据 [SemesterCalendarDetail.notes] 构建 `row → [IndexedNote]` 映射，保持列表内顺序。
 *
 * 附带全局 index 是为了让视图层在多条同内容备注时也能稳定取到各自的 ordinal。
 */
internal fun groupNotes(notes: List<SemesterCalendarNote>): Map<Int, List<IndexedNote>> =
    notes.withIndex()
        .map { (idx, note) -> IndexedNote(globalIndex = idx, note = note) }
        .groupBy { it.note.row }

/**
 * 为每个 [WeekRow] 计算"教学周序号"（1-based）。
 *
 * 规则：
 *  - 以 `semesterStart` 所在周的周一为"教学周 1"
 *  - 该周之后的每一周依次为 2, 3, 4...（**不论是否在 customWeekRange 内**，服务端
 *    若把中间某周标为"假期"，仍会占用一个教学周号，这是学期内常见的"停课周"语义）
 *  - 开学前的周（准备周/假期等）返回 `null`，UI 侧会改显示该 row 的 customWeekRange 标签
 *
 * @return `校历 row → 教学周序号`；开学前的 row 不在 map 中
 */
internal fun buildTeachingWeekIndex(
    weeks: List<WeekRow>,
    semesterStartMs: Long?
): Map<Int, Int> {
    if (semesterStartMs == null) return emptyMap()
    // 找到 semesterStart 所在周
    val startRowIndex = weeks.indexOfFirst { row ->
        row.startMs != null && row.endMs != null && semesterStartMs in row.startMs..row.endMs
    }
    if (startRowIndex < 0) return emptyMap()

    val result = HashMap<Int, Int>(weeks.size - startRowIndex)
    var teachingWeek = 1
    for (i in startRowIndex..weeks.lastIndex) {
        result[weeks[i].row] = teachingWeek
        teachingWeek += 1
    }
    return result
}
