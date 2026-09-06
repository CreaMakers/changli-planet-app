package com.zhelearn.CSUSTPlanet.common.cache

import android.os.Build
import androidx.annotation.RequiresApi
import com.zhelearn.CSUSTPlanet.feature.calendar.data.local.SemesterCalendarCache
import com.zhelearn.CSUSTPlanet.feature.calendar.data.repository.SemesterCalendarRepository
import com.dcelysia.csust_spider.education.data.remote.EducationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
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

    /** 串行化开学日期的首次拉取，避免多个页面同时未命中缓存时重复请求教务。 */
    private val termStartDateFetchMutex = Mutex()

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
     * 兼容字段：App 冷启动时间戳（由 [com.zhelearn.CSUSTPlanet.core.MainActivity] 写入）。
     *
     * 项目内无读点，保留避免破坏历史调用；未来建议统一走
     * [com.zhelearn.CSUSTPlanet.core.PlanetApplication.startTime]。
     */
    @Deprecated(
        "使用 PlanetApplication.startTime 代替",
        ReplaceWith("com.zhelearn.CSUSTPlanet.core.PlanetApplication.startTime")
    )
    var startTime: Long = 0L

    /**
     * 计算当前学期代码（统一规则，全 App 共用）。
     *
     * 优先按**已知的开学日期**判断：合并内置 [termMap] 与已缓存的学期列表，
     * 选取"开学日不晚于现在、且仍在教学周区间（[MAX_TEACHING_WEEK] 周内）"的
     * 最近一个学期。唯有当没有任何已知学期覆盖当前日期时，才回退到基于月份的
     * 兜底规则，避免在两个学期的交界处提前跳到下一学期。
     *
     * 月份兜底规则（基于上海时区）：
     *  - 7-12 月 → `当年-下一年-1`（秋季）
     *  - 2-6 月 → `上一年-当年-2`（春季）
     *  - 1 月   → `上一年-当年-1`（上学年延续）
     *
     * @param targetDate 需要查询的日期，默认今天；首页预览可传明天。
     * @return 形如 `"2025-2026-1"` 的学期代码
     */
    fun getCurrentTerm(targetDate: Date = Date()): String {
        findTeachingTermForDate(targetDate)?.let { return it }
        val calendar = Calendar.getInstance(shanghaiTz).apply { time = targetDate }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        return when {
            month >= 7 -> "$year-${year + 1}-1"
            month >= 2 -> "${year - 1}-${year}-2"
            else -> "${year - 1}-${year}-1"
        }
    }

    /** 按目标日期选择仍在教学周内的学期；全部过期时由调用方按月份兜底。 */
    private fun findTeachingTermForDate(targetDate: Date): String? {
        val cachedTerms = SemesterCalendarRepository.getListSync()
        val merged = HashMap<String, String>(termMap).apply {
            // 教务单独保存的日期也参与选择，不依赖服务端校历列表是否已加载。
            putAll(SemesterCalendarCache.getCachedTermStartDates())
            cachedTerms?.forEach { item ->
                val start = SemesterCalendarRepository.getTermStartDate(item.semesterCode)
                if (!start.isNullOrBlank()) put(item.semesterCode, start)
            }
        }
        val selectedTerm = merged.entries.asSequence()
            .filter { (_, start) ->
                computeWeekNumber(start, targetDate)?.let { it in 1..MAX_TEACHING_WEEK } == true
            }
            .maxByOrNull { it.value }?.key
        return selectedTerm
    }

    // ---------------- 开学日期（统一入口） ----------------

    /**
     * 统一获取学期开学日期（[TERM_DATE_TIME_FORMAT] 格式）。
     *
     * 查找顺序：内存/本地缓存 → 内置 [termMap]；均未命中返回 null。
     *
     * 本方法**不会触发网络请求**（保持同步行为，适合 Widget、ViewModel init 等场景调用）；
     * 如需拉取最新开学日，调用挂起的 [fetchTermStartDate]。
     *
     * @param term 学期代码，如 `"2024-2025-1"`
     * @return 开学日期字符串，或 null（未命中）
     */
    fun getTermStartDate(term: String): String? {
        if (term.isBlank()) return null
        SemesterCalendarRepository.getTermStartDate(term)?.let { return it }
        return termMap[term]
    }

    /**
     * 确保学期开学日期已缓存。
     *
     * 优先读取 [SemesterCalendarRepository] 的内存/MMKV 真实缓存；仅在缓存缺失时，
     * 才通过 [EducationHelper.getSemesterStartDate] 请求教务并将结果归一化、持久化。
     * 这里不使用 [getTermStartDate] 做缓存判断，因为它还包含内置 [termMap] 兜底值，
     * 兜底命中不能代表教务数据已经缓存。
     *
     * 首次请求通过互斥锁串行化，并在获得锁后再次检查缓存，避免总览页与课表页、
     * 或课表页内部多个协程同时发起重复请求。
     *
     * 在 IO 上执行；失败返回 null，不抛异常。
     *
     * @return 开学日（`yyyy-MM-dd HH:mm:ss`），或 null
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchTermStartDate(term: String): String? {
        if (term.isBlank()) return null
        SemesterCalendarRepository.getTermStartDate(term)?.let { return it }

        return termStartDateFetchMutex.withLock {
            SemesterCalendarRepository.getTermStartDate(term)?.let { return@withLock it }

            withContext(Dispatchers.IO) {
                runCatching {
                    val dateStr = EducationHelper.getSemesterStartDate(term) ?: return@withContext null

                    // 1. 将 "yyyy-MM-dd" 格式的字符串解析为 LocalDate
                    val localDate = LocalDate.parse(dateStr)

                    // 2. 往后加一天
                    val nextDay = localDate.plusDays(1)

                    // 3. 拼接时间（LocalDate 默认的 toString() 格式就是 yyyy-MM-dd）
                    val formatted = "$nextDay 00:00:00"

                    SemesterCalendarCache.saveTermStartDate(term, formatted)
                    formatted
                }.getOrNull()
            }
        }
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
     * @param targetDate 需要计算周数的日期，默认今天。
     */
    fun getCurrentWeekInt(term: String, maxWeek: Int = MAX_TEACHING_WEEK, targetDate: Date = Date()): Int {
        val startTimeStr = getTermStartDate(term) ?: return 1
        val week = computeWeekNumber(startTimeStr, targetDate) ?: return 1
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
     * 与当前学期选择使用同一入口，避免继续显示已结束学期的周数。
     */
    fun getCurrentWeekAuto(): String = getCurrentWeek(getCurrentTerm())

    // ---------------- 内部工具 ----------------

    /**
     * 根据开学日期字符串计算目标日期是第几周（1-based）。
     * 开学日之前返回 0；解析失败返回 null。
     */
    private fun computeWeekNumber(startTimeStr: String, targetDate: Date = Date()): Int? {
        val startDate = parseTermDate(startTimeStr) ?: return null
        val targetDay = Calendar.getInstance(shanghaiTz).apply {
            time = targetDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val diffMs = targetDay.time - startDate.time
        if (diffMs < 0) return 0
        val days = diffMs / (1000L * 60 * 60 * 24)
        return (days / 7 + 1).toInt()
    }

    /**
     * 解析 [TERM_DATE_TIME_FORMAT] 或仅日期部分（前 10 字符）为 [Date]。
     */
    private fun parseTermDate(raw: String): Date? = runCatching {
        val sdf = SimpleDateFormat(TERM_DATE_FORMAT, Locale.CHINA).apply { timeZone = shanghaiTz }
        sdf.parse(raw.take(10))
    }.getOrNull()

}
