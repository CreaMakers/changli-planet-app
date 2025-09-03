package com.example.changli_planet_app.feature.common.data.local.mmkv

import android.content.Context
import com.example.changli_planet_app.feature.common.data.local.entity.ExamArrangement
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV

class ExamArrangementCache(private val context: Context) {
    init {
        MMKV.initialize(context)
    }

    private val mmkv = MMKV.mmkvWithID("content_cache")
    private val gson = Gson()

    fun saveExamArrangement(exams: List<ExamArrangement>) {
        mmkv.encode("exams", gson.toJson(exams))
    }

    fun getExamArrangement(): List<ExamArrangement>? {
        val json = mmkv.decodeString("exams") ?: return null
        val type = object : TypeToken<List<ExamArrangement>>() {}.type
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