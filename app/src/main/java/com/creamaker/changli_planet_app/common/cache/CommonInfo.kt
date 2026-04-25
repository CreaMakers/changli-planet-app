package com.creamaker.changli_planet_app.common.cache

import com.creamaker.changli_planet_app.feature.calendar.data.repository.SemesterCalendarRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * 学期与周次相关工具。
 *
 * 重构说明：
 *  - 原先的硬编码 [termMap] 仅作为**网络数据未就绪时的兜底**；
 *  - 所有业务统一通过 [getTermStartDate] 获取开学日期，内部按
 *    **内存/本地缓存（由 [SemesterCalendarRepository] 回写） → 内置 termMap** 顺序查找；
 *  - 学期切换规则、当前周计算也统一收敛到本 object，避免全仓库 5+ 份副本不一致。
 */
object CommonInfo {

    // ---------------- 常量 ----------------

    /** 与 termMap 保持一致的开学日期格式。 */
    const val TERM_DATE_TIME_FORMAT: String = "yyyy-MM-dd HH:mm:ss"

    /** 仅日期部分格式。 */
    const val TERM_DATE_FORMAT: String = "yyyy-MM-dd"

    /** 教学周默认上限（长理大部分学期为 20 周，假期周按 22 留 buffer）。 */
    const val MAX_TEACHING_WEEK: Int = 20

    private const val SHANGHAI_TZ_ID = "Asia/Shanghai"
    private val shanghaiTz: TimeZone get() = TimeZone.getTimeZone(SHANGHAI_TZ_ID)

    // ---------------- 内置 termMap（兜底） ----------------

    /**
     * 内置兜底学期开学日期（格式：[TERM_DATE_TIME_FORMAT]）。
     *
     * 仅在服务端尚未返回数据、本地也无缓存时作为 fallback；
     * 后续可随服务端上线的学期逐步移除。
     */
    val termMap: Map<String, String> = mapOf(
        "2025-2026-2" to "2026-03-09 00:00:00",
        "2025-2026-1" to "2025-09-08 00:00:00",
        "2024-2025-2" to "2025-02-24 00:00:00",
        "2024-2025-1" to "2024-09-02 00:00:00",
        "2023-2024-2" to "2024-02-26 00:00:00",
        "2023-2024-1" to "2023-09-04 00:00:00",
        "2022-2023-2" to "2023-02-20 00:00:00",
        "2022-2023-1" to "2022-08-29 00:00:00",
        "2021-2022-2" to "2022-02-21 00:00:00",
        "2021-2022-1" to "2021-09-06 00:00:00",
        "2020-2021-2" to "2021-03-01 00:00:00",
        "2020-2021-1" to "2020-08-24 00:00:00",
        "2019-2020-2" to "2020-02-17 00:00:00",
        "2019-2020-1" to "2019-09-02 00:00:00"
    )

    // ---------------- 当前学期（统一入口） ----------------

    /**
     * 兼容字段：App 冷启动时间戳（由 [com.creamaker.changli_planet_app.core.MainActivity] 写入）。
     *
     * 项目内无读点，保留避免破坏历史调用；未来建议统一走
     * [com.creamaker.changli_planet_app.core.PlanetApplication.startTime]。
     */
    @Deprecated(
        "使用 PlanetApplication.startTime 代替",
        ReplaceWith("com.creamaker.changli_planet_app.core.PlanetApplication.startTime")
    )
    var startTime: Long = 0L

    /**
     * 计算当前学期代码（统一规则，全 App 共用）。
     *
     * 切换规则（基于上海时区）：
     *  - 7-12 月 → `当年-下一年-1`（秋季）
     *  - 2-6 月 → `上一年-当年-2`（春季）
     *  - 1 月   → `上一年-当年-1`（上学年延续）
     *
     * @return 形如 `"2025-2026-1"` 的学期代码
     */
    fun getCurrentTerm(): String {
        val calendar = Calendar.getInstance(shanghaiTz)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        return when {
            month >= 7 -> "$year-${year + 1}-1"
            month >= 2 -> "${year - 1}-${year}-2"
            else -> "${year - 1}-${year}-1"
        }
    }

    // ---------------- 开学日期（统一入口） ----------------

    /**
     * 统一获取学期开学日期（[TERM_DATE_TIME_FORMAT] 格式）。
     *
     * 查找顺序：内存/本地缓存 → 内置 [termMap]；均未命中返回 null。
     *
     * 本方法**不会触发网络请求**（保持同步行为，适合 Widget、ViewModel init 等场景调用）；
     * 如果需要主动刷新，请调用
     * [SemesterCalendarRepository.prefetchDetailIfMissing] 或
     * [SemesterCalendarRepository.getDetail]。
     *
     * @param term 学期代码，如 `"2024-2025-1"`
     * @return 开学日期字符串，或 null（未命中）
     */
    fun getTermStartDate(term: String): String? {
        if (term.isBlank()) return null
        return SemesterCalendarRepository.getTermStartDate(term) ?: termMap[term]
    }

    // ---------------- 当前周（文本 & 数值统一入口） ----------------

    /**
     * 获取当前是开学第几周（文本形式）。
     *
     * @param currentTerm 当前学期代码
     * @return `"第 N 周"` / `"未开学"` / `"未知"` / `"计算错误"`
     */
    fun getCurrentWeek(currentTerm: String): String {
        val startTimeStr = getTermStartDate(currentTerm) ?: return "未知"
        val weekNumber = computeWeekNumber(startTimeStr) ?: return "计算错误"
        if (weekNumber <= 0) return "未开学"
        return "第 $weekNumber 周"
    }

    /**
     * 获取当前是开学第几周（数值形式）。
     *
     * 与 [getCurrentWeek] 共用底层算法，结果被 clamp 到 `1..[maxWeek]` 区间。
     * 未命中学期时返回 1（与历史业务保持兼容，避免上游崩溃）。
     *
     * @param term 学期代码
     * @param maxWeek 上限，默认 [MAX_TEACHING_WEEK]
     */
    fun getCurrentWeekInt(term: String, maxWeek: Int = MAX_TEACHING_WEEK): Int {
        val startTimeStr = getTermStartDate(term) ?: return 1
        val week = computeWeekNumber(startTimeStr) ?: return 1
        return week.coerceIn(1, maxWeek)
    }

    /**
     * 判断学期是否已经开学（今天 >= 开学日）。未命中学期按"已开学"处理。
     */
    fun hasTermStarted(term: String): Boolean {
        val startTimeStr = getTermStartDate(term) ?: return true
        return runCatching {
            val startDate = parseTermDate(startTimeStr) ?: return true
            !Date().before(startDate)
        }.getOrDefault(true)
    }

    /**
     * 自动获取当前学期的周数（给 Widget / 启动路径快速调用）。
     *
     * 性能说明：[TERM_DATE_TIME_FORMAT] 格式可直接按字典序比较，
     * 无需 SimpleDateFormat.parse，避免 Widget 主线程频繁解析。
     */
    fun getCurrentWeekAuto(): String {
        val nowStr = formatNowForTermComparison()

        // 合并内置 termMap 与已缓存的学期列表（缓存优先覆盖）
        val merged = HashMap<String, String>(termMap.size + 4).apply {
            putAll(termMap)
            SemesterCalendarRepository.getListSync()?.forEach { item ->
                val start = SemesterCalendarRepository.getTermStartDate(item.semesterCode)
                if (!start.isNullOrBlank()) put(item.semesterCode, start)
            }
        }

        val best = merged.entries
            .asSequence()
            .sortedByDescending { it.value }
            .firstOrNull { it.value <= nowStr }
            ?: return "未开学"

        return getCurrentWeek(best.key)
    }

    // ---------------- 内部工具 ----------------

    /**
     * 根据开学日期字符串计算"今天是第几周（1-based）"。
     * 开学日之前返回 0；解析失败返回 null。
     */
    private fun computeWeekNumber(startTimeStr: String): Int? {
        val startDate = parseTermDate(startTimeStr) ?: return null
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val diffMs = today.time - startDate.time
        if (diffMs < 0) return 0
        val days = diffMs / (1000L * 60 * 60 * 24)
        return (days / 7 + 1).toInt()
    }

    /**
     * 解析 [TERM_DATE_TIME_FORMAT] 或仅日期部分（前 10 字符）为 [Date]。
     */
    private fun parseTermDate(raw: String): Date? = runCatching {
        val sdf = SimpleDateFormat(TERM_DATE_FORMAT, Locale.CHINA)
        sdf.parse(raw.take(10))
    }.getOrNull()

    private fun formatNowForTermComparison(): String =
        SimpleDateFormat(TERM_DATE_TIME_FORMAT, Locale.getDefault()).format(Date())
}
