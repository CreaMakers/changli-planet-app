package com.creamaker.changli_planet_app.feature.common.data.local.mmkv

import com.dcelysia.csust_spider.education.data.remote.model.ExamArrange
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV

class ExamArrangementCache {

    private val mmkv = MMKV.mmkvWithID("content_cache")
    private val gson = Gson()

    fun saveExamArrangement(exams: List<ExamArrange>) {
        mmkv.encode("exams", gson.toJson(exams))
    }

    fun getExamArrangement(): List<ExamArrange>? {
        val json = mmkv.decodeString("exams") ?: return null
        val type = object : TypeToken<List<ExamArrange>>() {}.type
        return try {
            val exams: List<ExamArrange>? = gson.fromJson(json, type)
            if (exams.isNullOrEmpty()) {
                mmkv.removeValueForKey("exams")
                return null
            }
            val valid = exams.filter { it.isCacheCompatible() }
            if (valid.size != exams.size) {
                mmkv.removeValueForKey("exams")
                return null
            }
            valid
        } catch (e: Exception) {
            mmkv.removeValueForKey("exams")
            null
        }
    }

    private fun ExamArrange.isCacheCompatible(): Boolean = runCatching {
        courseNameval.isNotBlank() && examTime.isNotBlank() && campus.isNotBlank() && examRoomval.isNotBlank()
    }.getOrDefault(false)

    fun clearCache() {
        mmkv.clearAll()
    }
}