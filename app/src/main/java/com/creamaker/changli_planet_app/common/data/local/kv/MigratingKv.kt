package com.creamaker.changli_planet_app.common.data.local.kv

import android.util.Log
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.tencent.mmkv.MMKV
import io.fastkv.FastKV

/**
 * Read from FastKV first, fallback to MMKV, and backfill FastKV when MMKV has value.
 * Write operations target FastKV only.
 */
class MigratingKv(
    private val cacheId: String,
    legacyStoresProvider: () -> List<MMKV> = { listOf(MMKV.mmkvWithID(cacheId)) }
) {
    companion object {
        private const val TAG = "MigratingKv"
    }

    @Volatile
    private var fastKvInstance: FastKV? = null

    private val legacyStores: List<MMKV> by lazy(legacyStoresProvider)

    private fun fastKv(): FastKV? {
        fastKvInstance?.let { return it }
        val context = runCatching { PlanetApplication.appContext }.getOrNull() ?: return null
        return runCatching { FastKV.Builder(context, cacheId).build() }
            .onFailure { Log.e(TAG, "Failed to build FastKV for cacheId=$cacheId", it) }
            .getOrNull()
            ?.also { fastKvInstance = it }
    }

    fun putString(key: String, value: String?) {
        runCatching { fastKv()?.putString(key, value) }
            .onFailure { Log.e(TAG, "Failed to write string key=$key in cacheId=$cacheId", it) }
    }

    fun getString(key: String, defaultValue: String? = null): String? {
        val fastKv = fastKv()
        if (runCatching { fastKv?.contains(key) }.getOrDefault(false) == true) {
            return runCatching { fastKv?.getString(key, defaultValue) }
                .onFailure { Log.e(TAG, "Failed to read string key=$key in cacheId=$cacheId", it) }
                .getOrNull() ?: defaultValue
        }

        legacyStores.forEach { legacy ->
            if (!legacy.containsKey(key)) return@forEach
            val value = legacy.decodeString(key, defaultValue)
            if (value != null) {
                runCatching { fastKv?.putString(key, value) }
                    .onFailure { Log.e(TAG, "Failed to backfill string key=$key in cacheId=$cacheId", it) }
            }
            return value
        }
        return defaultValue
    }

    fun putBoolean(key: String, value: Boolean) {
        runCatching { fastKv()?.putBoolean(key, value) }
            .onFailure { Log.e(TAG, "Failed to write boolean key=$key in cacheId=$cacheId", it) }
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val fastKv = fastKv()
        if (runCatching { fastKv?.contains(key) }.getOrDefault(false) == true) {
            return runCatching { fastKv?.getBoolean(key, defaultValue) }
                .onFailure { Log.e(TAG, "Failed to read boolean key=$key in cacheId=$cacheId", it) }
                .getOrNull() ?: defaultValue
        }

        legacyStores.forEach { legacy ->
            if (!legacy.containsKey(key)) return@forEach
            val value = legacy.decodeBool(key, defaultValue)
            runCatching { fastKv?.putBoolean(key, value) }
                .onFailure { Log.e(TAG, "Failed to backfill boolean key=$key in cacheId=$cacheId", it) }
            return value
        }
        return defaultValue
    }

    fun putInt(key: String, value: Int) {
        runCatching { fastKv()?.putInt(key, value) }
            .onFailure { Log.e(TAG, "Failed to write int key=$key in cacheId=$cacheId", it) }
    }

    fun getInt(key: String, defaultValue: Int): Int {
        val fastKv = fastKv()
        if (runCatching { fastKv?.contains(key) }.getOrDefault(false) == true) {
            return runCatching { fastKv?.getInt(key, defaultValue) }
                .onFailure { Log.e(TAG, "Failed to read int key=$key in cacheId=$cacheId", it) }
                .getOrNull() ?: defaultValue
        }

        legacyStores.forEach { legacy ->
            if (!legacy.containsKey(key)) return@forEach
            val value = legacy.decodeInt(key, defaultValue)
            runCatching { fastKv?.putInt(key, value) }
                .onFailure { Log.e(TAG, "Failed to backfill int key=$key in cacheId=$cacheId", it) }
            return value
        }
        return defaultValue
    }

    fun putLong(key: String, value: Long) {
        runCatching { fastKv()?.putLong(key, value) }
            .onFailure { Log.e(TAG, "Failed to write long key=$key in cacheId=$cacheId", it) }
    }

    fun getLong(key: String, defaultValue: Long): Long {
        val fastKv = fastKv()
        if (runCatching { fastKv?.contains(key) }.getOrDefault(false) == true) {
            return runCatching { fastKv?.getLong(key, defaultValue) }
                .onFailure { Log.e(TAG, "Failed to read long key=$key in cacheId=$cacheId", it) }
                .getOrNull() ?: defaultValue
        }

        legacyStores.forEach { legacy ->
            if (!legacy.containsKey(key)) return@forEach
            val value = legacy.decodeLong(key, defaultValue)
            runCatching { fastKv?.putLong(key, value) }
                .onFailure { Log.e(TAG, "Failed to backfill long key=$key in cacheId=$cacheId", it) }
            return value
        }
        return defaultValue
    }

    fun putFloat(key: String, value: Float) {
        runCatching { fastKv()?.putFloat(key, value) }
            .onFailure { Log.e(TAG, "Failed to write float key=$key in cacheId=$cacheId", it) }
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        val fastKv = fastKv()
        if (runCatching { fastKv?.contains(key) }.getOrDefault(false) == true) {
            return runCatching { fastKv?.getFloat(key, defaultValue) }
                .onFailure { Log.e(TAG, "Failed to read float key=$key in cacheId=$cacheId", it) }
                .getOrNull() ?: defaultValue
        }

        legacyStores.forEach { legacy ->
            if (!legacy.containsKey(key)) return@forEach
            val value = legacy.decodeFloat(key, defaultValue)
            runCatching { fastKv?.putFloat(key, value) }
                .onFailure { Log.e(TAG, "Failed to backfill float key=$key in cacheId=$cacheId", it) }
            return value
        }
        return defaultValue
    }

    fun remove(key: String) {
        runCatching { fastKv()?.remove(key) }
            .onFailure { Log.e(TAG, "Failed to remove key=$key in cacheId=$cacheId", it) }
        legacyStores.forEach { it.removeValueForKey(key) }
    }

    fun clearAll() {
        runCatching { fastKv()?.clear() }
            .onFailure { Log.e(TAG, "Failed to clear FastKV cacheId=$cacheId", it) }
        legacyStores.forEach { it.clearAll() }
    }
}

