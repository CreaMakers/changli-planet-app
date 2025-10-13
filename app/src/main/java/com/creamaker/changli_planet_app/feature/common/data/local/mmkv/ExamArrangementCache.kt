package com.creamaker.changli_planet_app.feature.common.data.local.mmkv

import android.content.Context
import com.dcelysia.csust_spider.education.data.remote.model.ExamArrange
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV

class ExamArrangementCache(private val context: Context) {
    init {
        MMKV.initialize(context)
    }

    private val mmkv = MMKV.mmkvWithID("content_cache")
    private val gson = Gson()

    fun saveExamArrangement(exams: List<ExamArrange>) {
        mmkv.encode("exams", gson.toJson(exams))
    }

    fun getExamArrangement(): List<ExamArrange>? {
        val json = mmkv.decodeString("exams") ?: return null
        val type = object : TypeToken<List<ExamArrange>>() {}.type
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