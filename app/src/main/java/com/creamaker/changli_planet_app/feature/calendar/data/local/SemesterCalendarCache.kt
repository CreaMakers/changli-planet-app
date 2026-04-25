package com.creamaker.changli_planet_app.feature.calendar.data.local

import com.creamaker.changli_planet_app.feature.calendar.data.CalendarIsoUtils
import com.creamaker.changli_planet_app.feature.calendar.data.remote.dto.SemesterCalendarDetail
import com.creamaker.changli_planet_app.feature.calendar.data.remote.dto.SemesterCalendarListItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV

/**
 * 校历本地持久化（MMKV）。
 *
 * 分三部分存储：
 *  1. 列表（[KEY_LIST]）
 *  2. 各学期校历详情（[KEY_DETAIL_PREFIX] + semesterCode）
 *  3. 学期开学日期映射（[KEY_TERM_START_PREFIX] + semesterCode）——专供
 *     [com.creamaker.changli_planet_app.common.cache.CommonInfo] 快速读取
 *
 * 使用独立的 MMKV ID（[CACHE_ID]）而非和 Score/Exam 共用 `content_cache`，
 * 理由：校历数据跨学期持久化、不应随 `clearContentCache` 一起清除，
 * 用户切换账号、重装后仍可快速命中学期开学日。
 */
object SemesterCalendarCache {

    private const val CACHE_ID = "semester_calendar_cache"
    private const val KEY_LIST = "list"
    private const val KEY_DETAIL_PREFIX = "detail_"
    private const val KEY_TERM_START_PREFIX = "term_start_"
    private const val KEY_LIST_UPDATE_TIME = "list_update_time"
    private const val KEY_DETAIL_UPDATE_PREFIX = "detail_update_"

    private val mmkv by lazy { MMKV.mmkvWithID(CACHE_ID) }
    private val gson by lazy { Gson() }

    /** `List<SemesterCalendarListItem>` 的 TypeToken；单例缓存避免每次构造匿名对象。 */
    private val listItemType by lazy {
        object : TypeToken<List<SemesterCalendarListItem>>() {}.type
    }

    // ---------------- 列表 ----------------

    fun saveList(list: List<SemesterCalendarListItem>) {
        runCatching {
            mmkv.encode(KEY_LIST, gson.toJson(list))
            mmkv.encode(KEY_LIST_UPDATE_TIME, System.currentTimeMillis())
        }
    }

    fun getList(): List<SemesterCalendarListItem>? {
        val json = mmkv.decodeString(KEY_LIST) ?: return null
        return runCatching {
            gson.fromJson<List<SemesterCalendarListItem>>(json, listItemType)
        }.getOrNull()
    }

    fun getListUpdateTime(): Long = mmkv.decodeLong(KEY_LIST_UPDATE_TIME, 0L)

    // ---------------- 详情 ----------------

    fun saveDetail(detail: SemesterCalendarDetail) {
        val code = detail.semesterCode
        if (code.isBlank()) return
        runCatching {
            mmkv.encode(KEY_DETAIL_PREFIX + code, gson.toJson(detail))
            mmkv.encode(KEY_DETAIL_UPDATE_PREFIX + code, System.currentTimeMillis())
            // 同步保存开学日期（与内置 termMap 一致的字符串格式）
            CalendarIsoUtils.isoToTermStartDate(detail.semesterStart)?.let { saveTermStartDate(code, it) }
        }
    }

    fun getDetail(semesterCode: String): SemesterCalendarDetail? {
        val json = mmkv.decodeString(KEY_DETAIL_PREFIX + semesterCode) ?: return null
        return runCatching { gson.fromJson(json, SemesterCalendarDetail::class.java) }.getOrNull()
    }

    fun getDetailUpdateTime(semesterCode: String): Long =
        mmkv.decodeLong(KEY_DETAIL_UPDATE_PREFIX + semesterCode, 0L)

    // ---------------- 学期开学日期（单独 key，便于 CommonInfo 快速读取） ----------------

    fun saveTermStartDate(semesterCode: String, startDate: String) {
        if (semesterCode.isBlank() || startDate.isBlank()) return
        mmkv.encode(KEY_TERM_START_PREFIX + semesterCode, startDate)
    }

    fun getTermStartDate(semesterCode: String): String? {
        return mmkv.decodeString(KEY_TERM_START_PREFIX + semesterCode, null)
            ?.takeIf { it.isNotBlank() }
    }
}

