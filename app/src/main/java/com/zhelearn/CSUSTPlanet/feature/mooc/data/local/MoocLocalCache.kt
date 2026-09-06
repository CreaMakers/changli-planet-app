package com.zhelearn.CSUSTPlanet.feature.mooc.data.local

import com.zhelearn.CSUSTPlanet.feature.mooc.data.model.CourseItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV

object MoocLocalCache {
    private const val CACHE_ID = "mooc_local_cache"
    private const val KEY_COURSE_ITEMS = "course_items"
    private const val KEY_LAST_SUCCESSFUL_REFRESH_TIME = "last_successful_refresh_time"
    private const val KEY_LAST_SUCCESSFUL_REFRESH_STUDENT_ID = "last_successful_refresh_student_id"

    private val mmkv by lazy { MMKV.mmkvWithID(CACHE_ID) }
    private val gson by lazy { Gson() }

    fun saveCourseItems(items: List<CourseItem>) {
        mmkv.encode(KEY_COURSE_ITEMS, gson.toJson(items))
    }

    fun getCourseItems(): List<CourseItem> {
        val json = mmkv.decodeString(KEY_COURSE_ITEMS) ?: return emptyList()
        return runCatching {
            gson.fromJson<List<CourseItem>>(
                json,
                object : TypeToken<List<CourseItem>>() {}.type
            ).orEmpty()
        }.getOrDefault(emptyList())
    }

    fun markSuccessfulRefresh(studentId: String) {
        mmkv.encode(KEY_LAST_SUCCESSFUL_REFRESH_STUDENT_ID, studentId)
        mmkv.encode(KEY_LAST_SUCCESSFUL_REFRESH_TIME, System.currentTimeMillis())
    }

    fun getLastSuccessfulRefreshTime(studentId: String): Long {
        // 旧缓存或其他学号的缓存不能用于跳过当前账号的网络刷新。
        if (mmkv.decodeString(KEY_LAST_SUCCESSFUL_REFRESH_STUDENT_ID) != studentId) return 0L
        return mmkv.decodeLong(KEY_LAST_SUCCESSFUL_REFRESH_TIME, 0L)
    }

    fun clear() {
        mmkv.clearAll()
    }
}
