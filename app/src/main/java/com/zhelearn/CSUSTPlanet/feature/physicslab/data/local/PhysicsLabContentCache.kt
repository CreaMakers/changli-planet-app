package com.zhelearn.CSUSTPlanet.feature.physicslab.data.local

import com.dcelysia.csust_spider.physicsexperiment.data.remote.dto.PhysicsLabIndex
import com.dcelysia.csust_spider.physicsexperiment.data.remote.dto.PhysicsLabScore
import com.dcelysia.csust_spider.physicsexperiment.data.remote.dto.PhysicsLabTask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV

/**
 * 物理实验内容的本地缓存（MMKV）。key 都带学号前缀：平台返回的是"按人"的数据，
 * 换账号后旧账号的课表与成绩绝不能命中。
 *
 * 平台密码**不在这里** —— 那是库自己的凭据（MMKV id `physics_lab_cache`），App 不重复存。
 */
object PhysicsLabContentCache {

    private const val CACHE_ID = "physics_lab_content_cache"
    private const val KEY_INDEX_SUFFIX = "_index"
    private const val KEY_TASKS_SUFFIX = "_tasks"
    private const val KEY_TASKS_AT_SUFFIX = "_tasks_at"
    private const val KEY_SCORES_SUFFIX = "_scores"

    /** 实验安排的刷新窗口：与正课一致，窗口内进课表不再联网。 */
    private const val TASKS_TTL_MS = 48 * 60 * 60 * 1000L

    private val mmkv by lazy { MMKV.mmkvWithID(CACHE_ID) }
    private val gson by lazy { Gson() }

    private val taskListType by lazy { object : TypeToken<List<PhysicsLabTask>>() {}.type }
    private val scoreListType by lazy { object : TypeToken<List<PhysicsLabScore>>() {}.type }

    fun saveIndex(studentId: String, index: PhysicsLabIndex) {
        if (studentId.isBlank()) return
        runCatching { mmkv.encode(studentId + KEY_INDEX_SUFFIX, gson.toJson(index)) }
    }

    fun getIndex(studentId: String): PhysicsLabIndex? {
        if (studentId.isBlank()) return null
        val json = mmkv.decodeString(studentId + KEY_INDEX_SUFFIX) ?: return null
        return runCatching { gson.fromJson(json, PhysicsLabIndex::class.java) }.getOrNull()
    }

    fun saveTasks(studentId: String, tasks: List<PhysicsLabTask>) {
        if (studentId.isBlank()) return
        runCatching { mmkv.encode(studentId + KEY_TASKS_SUFFIX, gson.toJson(tasks)) }
        runCatching { mmkv.encode(studentId + KEY_TASKS_AT_SUFFIX, System.currentTimeMillis()) }
    }

    fun getTasks(studentId: String): List<PhysicsLabTask>? {
        if (studentId.isBlank()) return null
        val json = mmkv.decodeString(studentId + KEY_TASKS_SUFFIX) ?: return null
        return runCatching {
            gson.fromJson<List<PhysicsLabTask>>(json, taskListType)
        }.getOrNull()
    }

    fun saveScores(studentId: String, scores: List<PhysicsLabScore>) {
        if (studentId.isBlank()) return
        runCatching { mmkv.encode(studentId + KEY_SCORES_SUFFIX, gson.toJson(scores)) }
    }

    fun getScores(studentId: String): List<PhysicsLabScore>? {
        if (studentId.isBlank()) return null
        val json = mmkv.decodeString(studentId + KEY_SCORES_SUFFIX) ?: return null
        return runCatching {
            gson.fromJson<List<PhysicsLabScore>>(json, scoreListType)
        }.getOrNull()
    }

    /** 实验安排上次成功刷新的时间（0 = 从没刷过）。 */
    fun tasksUpdatedAt(studentId: String): Long =
        if (studentId.isBlank()) 0L
        else runCatching { mmkv.decodeLong(studentId + KEY_TASKS_AT_SUFFIX, 0L) }.getOrDefault(0L)

    /** 没缓存过、或已超过刷新窗口，才需要联网拉取。 */
    fun shouldRefreshTasks(studentId: String): Boolean =
        getTasks(studentId) == null ||
            System.currentTimeMillis() - tasksUpdatedAt(studentId) > TASKS_TTL_MS

    /** 仅清物理实验的缓存；平台密码不归这里管。 */
    fun clear() {
        mmkv.clearAll()
    }
}
