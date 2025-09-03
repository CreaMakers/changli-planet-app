package com.example.changli_planet_app.feature.common.data.local.mmkv

import com.example.changli_planet_app.feature.common.data.local.entity.Grade
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
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    fun clearCache() {
        mmkv.clearAll()
    }
}