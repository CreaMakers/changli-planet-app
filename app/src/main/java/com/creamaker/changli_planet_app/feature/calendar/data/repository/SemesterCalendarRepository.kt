package com.creamaker.changli_planet_app.feature.calendar.data.repository

import com.creamaker.changli_planet_app.feature.calendar.data.CalendarIsoUtils
import com.creamaker.changli_planet_app.feature.calendar.data.local.SemesterCalendarCache
import com.creamaker.changli_planet_app.feature.calendar.data.remote.api.SemesterCalendarApi
import com.creamaker.changli_planet_app.feature.calendar.data.remote.dto.SemesterCalendarDetail
import com.creamaker.changli_planet_app.feature.calendar.data.remote.dto.SemesterCalendarListItem
import com.creamaker.changli_planet_app.utils.RetrofitUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * 校历 Repository：**网络 -> 本地（MMKV） -> 内存** 三级存储。
 *
 * ## 对外 API 选择指南
 *
 * | 场景                                      | 应该调用                      |
 * |-------------------------------------------|-----------------------------|
 * | Widget / 同步路径（不允许挂起）             | [getListSync] / [getDetailSync] / [getTermStartDate] |
 * | ViewModel / 协程上下文（可挂起）            | [getList] / [getDetail]       |
 * | 课表/首页刷新时"顺手拉一下校历"（不关心结果） | [prefetchDetailIfMissing]     |
 *
 * Sync 版本仅命中内存/本地，**绝不触发网络**；suspend 版本在本地缺失或 [getDetail] `forceRefresh=true`
 * 时走网络，成功后回写 MMKV 与内存；失败时保留已有缓存不动（降级体验）。
 *
 * ## Result 语义
 * - `Result.success`：可能来自本地或网络（两者内容一致的前提下）
 * - `Result.failure`：网络失败且本地也无缓存（或调用方显式 forceRefresh）
 *
 * ## 可测试性
 * 若需单元测试，可用 [overrideApiForTest] 注入 mock 实例。
 */
object SemesterCalendarRepository {

    // ---------------- 依赖 ----------------

    private var apiOverride: SemesterCalendarApi? = null
    private val api: SemesterCalendarApi
        get() = apiOverride ?: defaultApi
    private val defaultApi by lazy {
        RetrofitUtils.instancePlanet.create(SemesterCalendarApi::class.java)
    }

    /** 仅测试用：注入 mock api，生产代码不要调用。传 null 则还原默认实现。 */
    @androidx.annotation.VisibleForTesting
    fun overrideApiForTest(api: SemesterCalendarApi?) {
        apiOverride = api
    }

    /**
     * 独立的后台 Scope，用于 fire-and-forget 的 prefetch 场景，
     * 避免影响调用方（如 Overview 首次刷新）的 coroutineScope 等待语义。
     *
     * 因是 App 级单例，进程退出时随 JVM 自然回收；业务侧不需要手动取消。
     */
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ---------------- 内存层 ----------------

    @Volatile
    private var memoryList: List<SemesterCalendarListItem>? = null
    private val memoryDetails = ConcurrentHashMap<String, SemesterCalendarDetail>()

    // ---------------- 列表 ----------------

    /** 同步获取校历列表：仅读内存/本地，不触发网络。 */
    fun getListSync(): List<SemesterCalendarListItem>? {
        memoryList?.let { return it }
        return SemesterCalendarCache.getList()?.also { memoryList = it }
    }

    /**
     * 获取校历列表。
     *
     * @param forceRefresh `true` 时强制走网络；默认本地命中即返回
     * @return [Result.success] 含最新或缓存数据；强制刷新失败会如实上报 [Result.failure]，便于 UI 给用户反馈
     */
    suspend fun getList(forceRefresh: Boolean = false): Result<List<SemesterCalendarListItem>> =
        withContext(Dispatchers.IO) {
            if (!forceRefresh) {
                getListSync()?.let { return@withContext Result.success(it) }
            }
            val remote = runCatching { api.getSemesterCalendars() }
            remote.onSuccess { list ->
                memoryList = list
                SemesterCalendarCache.saveList(list)
            }
            // 非强制刷新场景下，网络失败 + 有本地缓存 → 降级返回 success，保证冷启动不抹数据；
            // 强制刷新（用户主动点刷新）必须如实上报失败，否则 VM 无法给用户 toast 提示
            if (remote.isFailure && !forceRefresh) {
                getListSync()?.let { return@withContext Result.success(it) }
            }
            remote
        }

    // ---------------- 详情 ----------------

    /** 同步获取校历详情：仅读内存/本地，不触发网络。 */
    fun getDetailSync(semesterCode: String): SemesterCalendarDetail? {
        if (semesterCode.isBlank()) return null
        memoryDetails[semesterCode]?.let { return it }
        return SemesterCalendarCache.getDetail(semesterCode)?.also {
            memoryDetails[semesterCode] = it
        }
    }

    /**
     * 获取校历详情。
     *
     * @param semesterCode 学期代码（非空）
     * @param forceRefresh `true` 时强制走网络
     * @return [Result.success] 含最新或缓存详情；强制刷新失败会如实上报 [Result.failure]，便于 UI 给用户反馈
     */
    suspend fun getDetail(
        semesterCode: String,
        forceRefresh: Boolean = false
    ): Result<SemesterCalendarDetail> = withContext(Dispatchers.IO) {
        if (semesterCode.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("semesterCode is blank"))
        }
        if (!forceRefresh) {
            getDetailSync(semesterCode)?.let { return@withContext Result.success(it) }
        }
        val remote = runCatching { api.getSemesterCalendarDetail(semesterCode) }
        remote.onSuccess { detail ->
            memoryDetails[semesterCode] = detail
            SemesterCalendarCache.saveDetail(detail)
        }
        // 同 getList：仅非强制刷新场景下降级，避免吞掉用户主动刷新的失败
        if (remote.isFailure && !forceRefresh) {
            getDetailSync(semesterCode)?.let { return@withContext Result.success(it) }
        }
        remote
    }

    // ---------------- 学期开学日期（供 CommonInfo 调用） ----------------

    /**
     * 仅同步读取学期开始日期（`yyyy-MM-dd HH:mm:ss`），命中内存 -> 本地，未命中返回 null。
     *
     * **不会触发网络**，适合 Widget / ViewModel init 等主线程场景。
     */
    fun getTermStartDate(semesterCode: String): String? {
        if (semesterCode.isBlank()) return null
        memoryDetails[semesterCode]?.semesterStart?.let { iso ->
            CalendarIsoUtils.isoToTermStartDate(iso)?.let { return it }
        }
        return SemesterCalendarCache.getTermStartDate(semesterCode)
    }

    /**
     * 静默触发一次学期详情刷新，**fire-and-forget 语义**：
     *  - 立即返回，不阻塞调用方 coroutineScope；
     *  - 仅当本地无缓存时才真正发起网络请求；
     *  - 失败吞掉。
     *
     * 适合课表/首页等"顺手拉一下校历让开学日期尽快落盘"的场景。
     */
    fun prefetchDetailIfMissing(semesterCode: String) {
        if (semesterCode.isBlank()) return
        if (getDetailSync(semesterCode) != null) return
        backgroundScope.launch {
            runCatching { getDetail(semesterCode, forceRefresh = false) }
        }
    }

    /** 清空所有内存缓存（列表 + 详情）；MMKV 不动。仅用于调试或账号切换。 */
    fun clearMemoryCache() {
        memoryList = null
        memoryDetails.clear()
    }
}
