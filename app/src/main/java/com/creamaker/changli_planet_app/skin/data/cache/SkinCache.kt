package com.creamaker.changli_planet_app.skin.data.cache

import android.util.Log
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.tencent.mmkv.MMKV
import io.fastkv.FastKV

object SkinCache {
    private const val TAG = "SkinCache"
    private const val CACHE_ID = "skin_cache"
    private const val KEY_SKIN_PATH = "skin_path"
    private const val KEY_IS_USING_SKIN = "is_using_skin"

    private val mmkv by lazy {
        MMKV.mmkvWithID(CACHE_ID)
    }

    @Volatile
    private var fastKvInstance: FastKV? = null

    private fun fastKv(): FastKV? {
        fastKvInstance?.let { return it }
        val context = runCatching { PlanetApplication.appContext }.getOrNull() ?: return null
        return runCatching { FastKV.Builder(context, CACHE_ID).build() }
            .onFailure { Log.e(TAG, "Failed to build FastKV", it) }
            .getOrNull()
            ?.also { fastKvInstance = it }
    }

    private fun readStringWithMigration(key: String, defaultValue: String): String {
        val fastKv = fastKv()
        if (runCatching { fastKv?.contains(key) }.getOrDefault(false) == true) {
            return runCatching { fastKv?.getString(key, defaultValue) }
                .onFailure { Log.e(TAG, "Failed to read string key=$key", it) }
                .getOrNull() ?: defaultValue
        }

        if (!mmkv.containsKey(key)) {
            return defaultValue
        }

        val value = mmkv.decodeString(key, null) ?: return defaultValue
        runCatching { fastKv?.putString(key, value) }
            .onFailure { Log.e(TAG, "Failed to backfill string key=$key", it) }
        return value
    }

    private fun readBooleanWithMigration(key: String, defaultValue: Boolean): Boolean {
        val fastKv = fastKv()
        if (runCatching { fastKv?.contains(key) }.getOrDefault(false) == true) {
            return runCatching { fastKv?.getBoolean(key, defaultValue) }
                .onFailure { Log.e(TAG, "Failed to read boolean key=$key", it) }
                .getOrNull() ?: defaultValue
        }

        if (!mmkv.containsKey(key)) {
            return defaultValue
        }

        val value = mmkv.decodeBool(key, defaultValue)
        runCatching { fastKv?.putBoolean(key, value) }
            .onFailure { Log.e(TAG, "Failed to backfill boolean key=$key", it) }
        return value
    }

    fun saveAssetsName(skinPath: String) {
        runCatching { fastKv()?.putString(KEY_SKIN_PATH, skinPath) }
            .onFailure { Log.e(TAG, "Failed to save skin path", it) }
    }

    fun getAssetsName(): String {
        return readStringWithMigration(KEY_SKIN_PATH, "skin_default")
    }

    fun saveSkinDownloaded(skinName: String) {
        runCatching { fastKv()?.putBoolean(skinName, true) }
            .onFailure { Log.e(TAG, "Failed to save downloaded skin=$skinName", it) }
    }

    fun getSkinDownloaded(skinName: String): Boolean {
        return readBooleanWithMigration(skinName, false)
    }

    fun saveIsUsingSkin(skinName: String) {
        runCatching { fastKv()?.putString(KEY_IS_USING_SKIN, skinName) }
            .onFailure { Log.e(TAG, "Failed to save using skin=$skinName", it) }
    }

    fun getIsUsingSkin(): String {
        return readStringWithMigration(KEY_IS_USING_SKIN, "skin_default")
    }
}