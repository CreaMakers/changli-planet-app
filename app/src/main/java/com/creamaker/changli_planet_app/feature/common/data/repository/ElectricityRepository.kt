package com.creamaker.changli_planet_app.feature.common.data.repository

import com.creamaker.changli_planet_app.overview.data.local.OverviewLocalCache
import com.example.csustdataget.CampusCard.CampusCardHelper
import com.tencent.mmkv.MMKV

class ElectricityRepository {
    private val mmkv by lazy { MMKV.defaultMMKV() }

    fun getBinding(): ElectricityBinding? {
        val school = mmkv.decodeString(KEY_SCHOOL, DEFAULT_SCHOOL).orEmpty()
        val dorm = mmkv.decodeString(KEY_DORM, DEFAULT_DORM).orEmpty()
        val room = mmkv.decodeString(KEY_ROOM, "").orEmpty()
        if (school == DEFAULT_SCHOOL || dorm == DEFAULT_DORM || room.isBlank()) return null
        return ElectricityBinding(school = school, dorm = dorm, room = room)
    }

    fun hasBinding(): Boolean = getBinding() != null

    fun shouldAutoRefresh(): Boolean {
        val binding = getBinding() ?: return false
        val snapshot = OverviewLocalCache.getElectricitySnapshot() ?: return true
        if (snapshot.lastTime <= 0L) return true
        return System.currentTimeMillis() - snapshot.lastTime >= AUTO_REFRESH_INTERVAL_MS
    }

    suspend fun refreshIfNeeded(): ElectricityQueryResult? {
        if (!shouldAutoRefresh()) return getCachedResult()
        return query(force = false)
    }

    suspend fun query(force: Boolean): ElectricityQueryResult? {
        val binding = getBinding() ?: return null
        if (!force) {
            val snapshot = OverviewLocalCache.getElectricitySnapshot()
            if (snapshot != null && System.currentTimeMillis() - snapshot.lastTime < AUTO_REFRESH_INTERVAL_MS) {
                return getCachedResult()
            }
        }

        val response = CampusCardHelper.queryElectricity(
            binding.school,
            binding.dorm,
            processDormAndRoom(binding.dorm, binding.room)
        ) ?: return ElectricityQueryResult(rawValue = "无数据", numericValue = null, fromCache = false)

        val value = extractElectricityValue(response.toString())
        if (value != null) {
            OverviewLocalCache.saveElectricitySnapshot(value)
        }
        return ElectricityQueryResult(
            rawValue = response.toString(),
            numericValue = value,
            fromCache = false
        )
    }

    fun saveBinding(school: String, dorm: String, room: String) {
        mmkv.encode(KEY_SCHOOL, school)
        mmkv.encode(KEY_DORM, dorm)
        mmkv.encode(KEY_ROOM, room)
    }

    fun getCachedResult(): ElectricityQueryResult? {
        val snapshot = OverviewLocalCache.getElectricitySnapshot() ?: return null
        return ElectricityQueryResult(
            rawValue = snapshot.lastValue.toString(),
            numericValue = snapshot.lastValue,
            fromCache = true
        )
    }

    private fun extractElectricityValue(rawValue: String): Float? {
        val regex = Regex("(\\d*\\.?\\d+)")
        return regex.find(rawValue)?.value?.toFloatOrNull()
    }

    private fun processDormAndRoom(dorm: String, room: String): String {
        val containsA = dorm.contains('A')
        val containsB = dorm.contains('B')
        val roomContainsLetter = room.any { it.isLetter() }

        return when {
            containsA && !roomContainsLetter -> "A$room"
            containsB && !roomContainsLetter -> "B$room"
            else -> room
        }
    }

    data class ElectricityBinding(
        val school: String,
        val dorm: String,
        val room: String
    )

    data class ElectricityQueryResult(
        val rawValue: String,
        val numericValue: Float?,
        val fromCache: Boolean
    )

    companion object {
        private const val KEY_SCHOOL = "school"
        private const val KEY_DORM = "dor"
        private const val KEY_ROOM = "door_number"
        private const val DEFAULT_SCHOOL = "选择校区"
        private const val DEFAULT_DORM = "选择宿舍楼"
        const val AUTO_REFRESH_INTERVAL_MS = 10 * 60 * 1000L
    }
}
