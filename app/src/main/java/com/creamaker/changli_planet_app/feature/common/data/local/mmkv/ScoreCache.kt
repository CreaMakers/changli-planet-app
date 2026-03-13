package com.creamaker.changli_planet_app.feature.common.data.local.mmkv

import com.creamaker.changli_planet_app.feature.common.data.local.entity.Grade
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV

object ScoreCache {

    private val mmkv by lazy { MMKV.mmkvWithID("content_cache") }
    private val gson = Gson()

    fun saveGrades(grades: List<Grade>) {
        val normalized = grades.mapNotNull(::normalizeGrade)
        if (normalized.isEmpty()) {
            mmkv.removeValueForKey("grades")
            return
        }
        mmkv.encode("grades", gson.toJson(normalized))
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
            val normalized = grades.mapNotNull(::normalizeGrade)
            if (normalized.isEmpty()) {
                mmkv.removeValueForKey("grades")
                return null
            }
            if (normalized.size != grades.size) {
                mmkv.encode("grades", gson.toJson(normalized))
            }
            normalized
        } catch (e: Exception) {
            mmkv.removeValueForKey("grades")
            null
        }
    }

    private fun normalizeGrade(grade: Grade): Grade? {
        val normalizedName = grade.name.trim()
        val normalizedGrade = grade.grade.trim()
        if (normalizedName.isBlank() || normalizedGrade.isBlank()) return null

        val normalizedItem = grade.item.trim().ifBlank { "未知学期" }
        val normalizedId = grade.id.trim().ifBlank { "${normalizedItem}_${normalizedName}" }

        return grade.copy(
            id = normalizedId,
            item = normalizedItem,
            name = normalizedName,
            grade = normalizedGrade,
            score = grade.score.trim().ifBlank { "0" },
            point = grade.point.trim().ifBlank { "0" },
            timeR = grade.timeR.trim(),
            flag = grade.flag.trim(),
            upperReItem = grade.upperReItem.trim(),
            method = grade.method.trim(),
            property = grade.property.trim(),
            attribute = grade.attribute.trim(),
            reItem = grade.reItem.trim()
        )
    }

    fun clearCache() {
        mmkv.clearAll()
    }
}
