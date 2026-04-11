package com.creamaker.changli_planet_app.overview.data.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV

object OverviewLocalCache {
    private const val CACHE_ID = "overview_local_cache"
    private const val KEY_ELECTRICITY_HISTORY = "electricity_history"
    private const val KEY_LAST_ELECTRICITY = "last_electricity"
    private const val KEY_PREV_ELECTRICITY = "prev_electricity"
    private const val KEY_LAST_ELECTRICITY_TIME = "last_electricity_time"
    private const val KEY_PREV_ELECTRICITY_TIME = "prev_electricity_time"
    private const val MAX_ELECTRICITY_HISTORY_SIZE = 20
    private val mmkv by lazy { MMKV.mmkvWithID(CACHE_ID) }
    private val gson by lazy { Gson() }

    fun saveElectricitySnapshot(value: Float) {
        appendElectricityHistory(value)
        val oldValue = mmkv.decodeFloat(KEY_LAST_ELECTRICITY, Float.NaN)
        val oldTime = mmkv.decodeLong(KEY_LAST_ELECTRICITY_TIME, 0L)
        if (!oldValue.isNaN() && oldTime > 0L) {
            mmkv.encode(KEY_PREV_ELECTRICITY, oldValue)
            mmkv.encode(KEY_PREV_ELECTRICITY_TIME, oldTime)
        }
        mmkv.encode(KEY_LAST_ELECTRICITY, value)
        mmkv.encode(KEY_LAST_ELECTRICITY_TIME, System.currentTimeMillis())
    }

    fun getElectricitySnapshot(): ElectricitySnapshot? {
        val lastValue = mmkv.decodeFloat(KEY_LAST_ELECTRICITY, Float.NaN)
        val lastTime = mmkv.decodeLong(KEY_LAST_ELECTRICITY_TIME, 0L)
        if (lastValue.isNaN() || lastTime <= 0L) return null

        val prevValue = mmkv.decodeFloat(KEY_PREV_ELECTRICITY, Float.NaN)
        val prevTime = mmkv.decodeLong(KEY_PREV_ELECTRICITY_TIME, 0L)
        return ElectricitySnapshot(
            lastValue = lastValue,
            lastTime = lastTime,
            previousValue = prevValue.takeUnless { it.isNaN() },
            previousTime = prevTime.takeIf { it > 0L },
            history = getElectricityHistory()
        )
    }

    fun getElectricityHistory(): List<ElectricityHistoryEntry> {
        val json = mmkv.decodeString(KEY_ELECTRICITY_HISTORY) ?: return emptyList()
        return runCatching {
            gson.fromJson<List<ElectricityHistoryEntry>>(
                json,
                object : TypeToken<List<ElectricityHistoryEntry>>() {}.type
            ).orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun appendElectricityHistory(value: Float) {
        val now = System.currentTimeMillis()
        val history = getElectricityHistory().toMutableList()
        val last = history.lastOrNull()
        if (last != null && kotlin.math.abs(last.value - value) < 0.01f && now - last.timestamp < 10 * 60 * 1000L) {
            history[history.lastIndex] = last.copy(timestamp = now)
        } else {
            history += ElectricityHistoryEntry(value = value, timestamp = now)
        }
        val trimmed = history.takeLast(MAX_ELECTRICITY_HISTORY_SIZE)
        mmkv.encode(KEY_ELECTRICITY_HISTORY, gson.toJson(trimmed))
    }

    data class ElectricitySnapshot(
        val lastValue: Float,
        val lastTime: Long,
        val previousValue: Float?,
        val previousTime: Long?,
        val history: List<ElectricityHistoryEntry> = emptyList()
    )

    data class ElectricityHistoryEntry(
        val value: Float,
        val timestamp: Long
    )
}
