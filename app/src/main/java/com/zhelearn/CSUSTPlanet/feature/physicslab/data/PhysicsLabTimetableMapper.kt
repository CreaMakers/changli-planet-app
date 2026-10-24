package com.zhelearn.CSUSTPlanet.feature.physicslab.data

import com.dcelysia.csust_spider.physicsexperiment.data.remote.dto.PhysicsLabTask
import com.zhelearn.CSUSTPlanet.feature.common.data.local.entity.TimeTableMySubject
import kotlin.math.abs

/**
 * 把物理实验的一条安排映射成课表实体 [TimeTableMySubject]，复用课表的网格与卡片。
 *
 * 格子位置是**显示层就近近似**，不代表教务节次：实验给的是整块时间（`13:30 - 15:45`），
 * 网格是固定 10 节，两边对不齐，只能按重叠就近落格。平台原文时间另有
 * [TimeTableMySubject.time] 原样展示，两者不是一回事。
 *
 * 平台的 `hours` 不参与映射：它是平台的课时口径，与时间块跨的节次数不一致。
 *
 * @return 周次、星期或上课时间任一解析失败时返回 null（该条不进课表），本函数不抛异常。
 */
fun PhysicsLabTask.toTimeTableMySubject(
    term: String,
    studentId: String,
    studentPassword: String,
): TimeTableMySubject? {
    val week = WEEK_REGEX.find(weekday)?.groupValues?.get(1)?.toIntOrNull() ?: return null
    val dayOfWeek = WEEKDAY_REGEX.find(weekday)
        ?.groupValues?.get(1)
        ?.let(::weekdayToNumber)
        ?: return null
    val sections = resolveSections(time) ?: return null

    return TimeTableMySubject(
        courseName = courseName,
        classroom = location,
        teacher = teacher,
        weeks = listOf(week),
        start = sections.first,
        step = sections.second,
        weekday = dayOfWeek,
        // 合成负数 id，避开 Room 的自增主键
        id = syntheticId(),
        // 非用户自建课程，避免进入"删除自定义课程"流程
        isCustom = false,
        term = term,
        studentId = studentId,
        studentPassword = studentPassword,
    ).apply {
        // 精确时间放非持久化字段，供卡片与弹窗原样显示（与近似落格是两回事）
        this.time = this@toTimeTableMySubject.time.trim()
    }
}

/** `2026-09-25 13:30 - 15:45` → `13:30-15:45`（小时补零）；解析不出来时原样返回，不抛异常。 */
fun formatPhysicsLabTimeRange(raw: String): String {
    val match = TIME_RANGE_REGEX.find(raw) ?: return raw.trim()
    val hour1 = match.groupValues[1].padStart(2, '0')
    val minute1 = match.groupValues[2]
    val hour2 = match.groupValues[3].padStart(2, '0')
    val minute2 = match.groupValues[4]
    return "$hour1:$minute1-$hour2:$minute2"
}

/**
 * 卡片上的两行时间：`13:30` / `–15:45`。格子只有约 50dp，单行会被截断成 `13:30…`；
 * 第二行带连接号，避免被读成两个时间。解析失败返回 null，调用方退回单行写法。
 */
fun formatPhysicsLabTimeRangeLines(raw: String): Pair<String, String>? {
    val match = TIME_RANGE_REGEX.find(raw) ?: return null
    val start = match.groupValues[1].padStart(2, '0') + ":" + match.groupValues[2]
    val end = match.groupValues[3].padStart(2, '0') + ":" + match.groupValues[4]
    return start to "–$end"
}

/**
 * 卡片短项目名：去掉末尾校区括号与金盆岭批次的前导 `D`（`D不良导体热导率的测定(金盆岭校区）`
 * → `不良导体热导率的测定`）。前导字母**只去 `D`**，不能推广成"任意前导字母"——
 * 平台里有 `X射线衍射` 这类真名以字母开头，误伤会把名字改坏。去掉后为空则保留原名，
 * 详情弹窗仍显示全名。
 */
fun shortPhysicsLabTitle(raw: String): String {
    val trimmed = raw.trim()
    return trimmed.replace(TRAILING_CAMPUS_REGEX, "")
        .replace(LEADING_BATCH_MARK_REGEX, "")
        .trim()
        .ifBlank { trimmed }
}

/** 金盆岭批次项目名前面的单个 `D`（后面必须是中文或空格，避免误伤 `D-半乳糖…` 这类真实名称） */
private val LEADING_BATCH_MARK_REGEX = Regex("^D(?=[\\u4e00-\\u9fa5]|\\s)")

/** 末尾的"（xx校区xx）"，全角/半角括号都认 */
private val TRAILING_CAMPUS_REGEX = Regex("[（(][^（()）]*校区[^（()）]*[)）]\\s*$")

/** 卡片短地点：去掉开头的校区前缀（`金盆岭校区三号教学楼402` → `三号教学楼402`）；为空则保留原名。 */
fun shortPhysicsLabLocation(raw: String): String {
    val trimmed = raw.trim()
    return trimmed.replace(LEADING_CAMPUS_REGEX, "").trim().ifBlank { trimmed }
}

/** 开头的"xx校区"（到校区二字为止，不含标点） */
private val LEADING_CAMPUS_REGEX = Regex("^[^，,（()）]{1,10}?校区\\s*")

/** `第3周 星期五` → `3` */
private val WEEK_REGEX = Regex("第(\\d+)周")

/** `第3周 星期五` → `五` */
private val WEEKDAY_REGEX = Regex("星期([一二三四五六日天])")

/** `2026-09-25 13:30 - 15:45` → 起止时分 */
private val TIME_RANGE_REGEX = Regex("(\\d{1,2}):(\\d{2})\\s*[-~～—]\\s*(\\d{1,2}):(\\d{2})")

/** 课表 10 个节次的开始/结束时刻（从 0 点起的分钟数），1-based，与 UI 时间轴一一对应 */
private val SECTION_RANGES: List<Pair<Int, Int>> = listOf(
    480 to 525,   // 1  08:00-08:45
    535 to 580,   // 2  08:55-09:40
    610 to 655,   // 3  10:10-10:55
    665 to 710,   // 4  11:05-11:50
    840 to 885,   // 5  14:00-14:45
    895 to 940,   // 6  14:55-15:40
    970 to 1015,  // 7  16:10-16:55
    1025 to 1070, // 8  17:05-17:50
    1170 to 1215, // 9  19:30-20:15
    1225 to 1270, // 10 20:25-21:10
)

/** 兜底容差（分钟）：只在没有任何真实重叠节次时用于就近贴一节 */
private const val OVERLAP_TOLERANCE_MINUTES = 15

private fun weekdayToNumber(cn: String): Int = when (cn) {
    "一" -> 1
    "二" -> 2
    "三" -> 3
    "四" -> 4
    "五" -> 5
    "六" -> 6
    else -> 7 // 日 / 天
}

private fun minuteOfDay(hour: String, minute: String): Int? {
    val h = hour.toIntOrNull() ?: return null
    val m = minute.toIntOrNull() ?: return null
    if (h !in 0..23 || m !in 0..59) return null
    return h * 60 + m
}

/**
 * 把上课时间落到节次上：只算真实重叠，容差仅在没有真实重叠时兜底贴一节。
 *
 * @return `start to step`（1-based，step ≥ 1）；无法解析或完全贴不上时返回 null。
 */
private fun resolveSections(time: String): Pair<Int, Int>? {
    val match = TIME_RANGE_REGEX.find(time) ?: return null
    val groups = match.groupValues
    val startMinute = minuteOfDay(groups[1], groups[2]) ?: return null
    val endMinute = minuteOfDay(groups[3], groups[4]) ?: return null
    if (endMinute <= startMinute) return null

    val overlappedSections = SECTION_RANGES.withIndex()
        .filter { (_, range) -> overlapMinutes(startMinute, endMinute, range.first, range.second) > 0 }
        .map { it.index + 1 }

    if (overlappedSections.isEmpty()) {
        // 兜底：没有真实重叠时就近贴一节，超出容差则该条不进课表
        val nearest = SECTION_RANGES.withIndex()
            .map { (index, range) -> (index + 1) to gapMinutes(startMinute, endMinute, range.first, range.second) }
            .minByOrNull { it.second }
            ?: return null
        return if (nearest.second <= OVERLAP_TOLERANCE_MINUTES) nearest.first to 1 else null
    }

    val start = overlappedSections.first()
    var step = 1
    while (step < overlappedSections.size && overlappedSections[step] == start + step) {
        step++
    }
    return start to step
}

/** 两个时间区间的真实重叠分钟数，无重叠为 0 */
private fun overlapMinutes(aStart: Int, aEnd: Int, bStart: Int, bEnd: Int): Int =
    (minOf(aEnd, bEnd) - maxOf(aStart, bStart)).coerceAtLeast(0)

/** 两个时间区间的间隔分钟数，有重叠为 0 */
private fun gapMinutes(aStart: Int, aEnd: Int, bStart: Int, bEnd: Int): Int = when {
    bEnd <= aStart -> aStart - bEnd
    bStart >= aEnd -> bStart - aEnd
    else -> 0
}

/** 用"项目编号 + 批次"生成稳定的负数 id，保证非 0 */
private fun PhysicsLabTask.syntheticId(): Int {
    val hash = abs("$courseId-$batch".hashCode())
    return if (hash == 0) -1 else -hash
}
