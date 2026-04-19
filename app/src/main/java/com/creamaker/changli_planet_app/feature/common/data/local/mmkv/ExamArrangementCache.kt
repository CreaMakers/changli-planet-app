package com.creamaker.changli_planet_app.feature.common.data.local.mmkv

import com.creamaker.changli_planet_app.common.data.local.kv.MigratingKv
import com.dcelysia.csust_spider.education.data.remote.model.ExamArrange
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ExamArrangementCache {
    companion object {
        private const val CACHE_ID = "content_cache"
        private const val KEY_EXAMS = "exams"
    }

    private val kv = MigratingKv(CACHE_ID)
    private val gson = Gson()

    fun saveExamArrangement(exams: List<ExamArrange>) {
        kv.putString(KEY_EXAMS, gson.toJson(exams))
    }

    fun getExamArrangement(): List<ExamArrange>? {
        val json = kv.getString(KEY_EXAMS, null) ?: return null
        val type = object : TypeToken<List<ExamArrange>>() {}.type
        return try {
            val exams: List<ExamArrange>? = gson.fromJson(json, type)
            if (exams.isNullOrEmpty()) {
                kv.remove(KEY_EXAMS)
                return null
            }
            val valid = exams.filter { it.isCacheCompatible() }
            if (valid.size != exams.size) {
                kv.remove(KEY_EXAMS)
                return null
            }
            valid
        } catch (e: Exception) {
            kv.remove(KEY_EXAMS)
            null
        }
    }

    private fun ExamArrange.isCacheCompatible(): Boolean = runCatching {
        courseNameval.isNotBlank() && examTime.isNotBlank() && campus.isNotBlank() && examRoomval.isNotBlank()
    }.getOrDefault(false)

    fun clearCache() {
        kv.clearAll()
    }
}