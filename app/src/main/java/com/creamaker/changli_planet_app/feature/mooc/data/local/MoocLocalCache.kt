package com.creamaker.changli_planet_app.feature.mooc.data.local

import com.dcelysia.csust_spider.mooc.data.remote.dto.MoocHomework
import com.dcelysia.csust_spider.mooc.data.remote.dto.MoocTest
import com.dcelysia.csust_spider.mooc.data.remote.dto.PendingAssignmentCourse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV

object MoocLocalCache {
    private const val CACHE_ID = "mooc_local_cache"
    private const val KEY_PENDING_COURSES = "pending_courses"
    private const val KEY_PENDING_HOMEWORKS = "pending_homeworks_by_course"
    private const val KEY_PENDING_TESTS = "pending_tests_by_course"

    private val mmkv by lazy { MMKV.mmkvWithID(CACHE_ID) }
    private val gson by lazy { Gson() }

    fun savePendingCourses(courses: List<PendingAssignmentCourse>) {
        mmkv.encode(KEY_PENDING_COURSES, gson.toJson(courses))
    }

    fun getPendingCourses(): List<PendingAssignmentCourse> {
        val json = mmkv.decodeString(KEY_PENDING_COURSES) ?: return emptyList()
        return runCatching {
            gson.fromJson<List<PendingAssignmentCourse>>(
                json,
                object : TypeToken<List<PendingAssignmentCourse>>() {}.type
            ).orEmpty()
        }.getOrDefault(emptyList())
    }

    fun savePendingHomeworksByCourse(homeworks: Map<String, List<MoocHomework>>) {
        mmkv.encode(KEY_PENDING_HOMEWORKS, gson.toJson(homeworks))
    }

    fun getPendingHomeworksByCourse(): Map<String, List<MoocHomework>> {
        val json = mmkv.decodeString(KEY_PENDING_HOMEWORKS) ?: return emptyMap()
        return runCatching {
            gson.fromJson<Map<String, List<MoocHomework>>>(
                json,
                object : TypeToken<Map<String, List<MoocHomework>>>() {}.type
            ).orEmpty()
        }.getOrDefault(emptyMap())
    }

    fun savePendingTestsByCourse(tests: Map<String, List<MoocTest>>) {
        mmkv.encode(KEY_PENDING_TESTS, gson.toJson(tests))
    }

    fun getPendingTestsByCourse(): Map<String, List<MoocTest>> {
        val json = mmkv.decodeString(KEY_PENDING_TESTS) ?: return emptyMap()
        return runCatching {
            gson.fromJson<Map<String, List<MoocTest>>>(
                json,
                object : TypeToken<Map<String, List<MoocTest>>>() {}.type
            ).orEmpty()
        }.getOrDefault(emptyMap())
    }
}
