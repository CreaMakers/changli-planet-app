package com.example.changli_planet_app.Cache

import android.content.Context
import com.example.changli_planet_app.Network.Response.Grade
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV

class ScoreCache(private val context: Context) {
    init {
        MMKV.initialize(context)
    }

    private val mmkv = MMKV.mmkvWithID("content_cache")
    private val gson = Gson()

    fun saveGrades(grades: List<Grade>) {
        mmkv.encode("grades", gson.toJson(grades))
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