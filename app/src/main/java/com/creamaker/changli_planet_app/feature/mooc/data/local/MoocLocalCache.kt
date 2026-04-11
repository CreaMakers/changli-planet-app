package com.creamaker.changli_planet_app.feature.mooc.data.local

import com.creamaker.changli_planet_app.feature.mooc.data.model.CourseItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV

object MoocLocalCache {
    private const val CACHE_ID = "mooc_local_cache"
    private const val KEY_COURSE_ITEMS = "course_items"
    private const val KEY_LAST_SUCCESSFUL_REFRESH_TIME = "last_successful_refresh_time"

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

    fun markSuccessfulRefresh() {
        mmkv.encode(KEY_LAST_SUCCESSFUL_REFRESH_TIME, System.currentTimeMillis())
    }

    fun getLastSuccessfulRefreshTime(): Long {
        return mmkv.decodeLong(KEY_LAST_SUCCESSFUL_REFRESH_TIME, 0L)
    }

    fun clear() {
        mmkv.clearAll()
    }
}
