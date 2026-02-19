package com.creamaker.changli_planet_app.feature.common.data.local.mmkv

import com.creamaker.changli_planet_app.feature.common.data.local.entity.Grade
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV

object ScoreCache {

    private val mmkv by lazy { MMKV.mmkvWithID("content_cache") }
    private val gson = Gson()

    fun saveGrades(grades: List<Grade>) {
        mmkv.encode("grades", gson.toJson(grades))
    }

    fun saveGradesDetailByUrl(url: String, details: String) {
        mmkv.encode(url, details)
    }

    fun getGradesDetailByUrl(url: String): String {
        return mmkv.getString(url, "") ?: ""
    }

    fun getGrades(): List<Grade>? {
        val json = mmkv.decodeString("grades") ?: return null
        val type = object : TypeToken<List<Grade>>() {}.type
        return try {
            val grades: List<Grade>? = gson.fromJson(json, type)
            if (grades == null) {
                mmkv.removeValueForKey("grades")
                return null
            }
            if (grades.any { !it.isCacheCompatible() }) {
                mmkv.removeValueForKey("grades")
                return null
            }
            grades
        } catch (e: Exception) {
            mmkv.removeValueForKey("grades")
            null
        }
    }

    private fun Grade.isCacheCompatible(): Boolean = runCatching {
        id.isNotBlank() && item.isNotBlank() && name.isNotBlank() && grade.isNotBlank()
    }.getOrDefault(false)

    fun clearCache() {
        mmkv.clearAll()
    }
}