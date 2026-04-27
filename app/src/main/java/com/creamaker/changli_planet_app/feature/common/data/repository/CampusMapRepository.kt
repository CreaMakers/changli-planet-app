package com.creamaker.changli_planet_app.feature.common.data.repository

import android.util.Log
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.feature.common.data.remote.api.CampusMapApi
import com.creamaker.changli_planet_app.feature.common.data.remote.dto.CampusMapGeoJson
import com.creamaker.changli_planet_app.utils.RetrofitUtils
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 校园地图数据仓库：负责网络拉取 + 本地 JSON 缓存。
 *
 * - 本地缓存目录：`filesDir/campus_map/map.json`
 * - 带显式 schema 版本号：覆盖安装后如果旧版本写入的 schema 与当前不兼容，直接丢弃缓存重新拉，
 *   避免出现"反序列化 null 字段 → 过滤空 → polygon 全不显示"这类 bug。
 */
object CampusMapRepository {

    private const val CACHE_DIR = "campus_map"
    private const val CACHE_FILE = "map.json"

    /** 本地缓存 schema 版本号。**改动 CampusMapDto 字段时必须同步 +1**。 */
    private const val CACHE_SCHEMA_VERSION = 1

    private const val TAG = "CampusMapRepo"

    private val api: CampusMapApi by lazy {
        RetrofitUtils.instancePlanet.create(CampusMapApi::class.java)
    }

    private val gson: Gson by lazy { Gson() }

    /** 缓存信封：用来区分旧裸 JSON（v0）和带版本号的新格式。 */
    private data class CacheEnvelope(
        @SerializedName("version") val version: Int,
        @SerializedName("data") val data: CampusMapGeoJson?,
    )

    /** 读本地缓存；读失败/不存在/版本不匹配/数据不合法时返回 null。 */
    suspend fun loadCache(): CampusMapGeoJson? = withContext(Dispatchers.IO) {
        val file = cacheFile() ?: return@withContext null
        if (!file.exists() || file.length() == 0L) return@withContext null
        val result = runCatching {
            val text = file.readText(Charsets.UTF_8)
            val envelope = gson.fromJson(text, CacheEnvelope::class.java)
            if (envelope?.version == CACHE_SCHEMA_VERSION && envelope.data.isValidForRender()) {
                envelope.data
            } else null
        }.getOrNull()
        if (result == null) {
            // 缓存不可用时主动清理，避免反复读到坏数据
            runCatching { file.delete() }
            Log.w(TAG, "cache discarded (version mismatch or corrupt)")
        }
        result
    }

    /** 拉取线上最新数据，并以当前 schema 版本写入本地缓存。 */
    suspend fun fetchRemoteAndCache(): CampusMapGeoJson = withContext(Dispatchers.IO) {
        val geoJson = api.getCampusMap()
        runCatching {
            cacheFile()?.apply {
                parentFile?.mkdirs()
                val envelope = CacheEnvelope(CACHE_SCHEMA_VERSION, geoJson)
                writeText(gson.toJson(envelope), Charsets.UTF_8)
            }
        } // 缓存写入失败不影响主流程
        geoJson
    }

    /**
     * 数据合法性校验：即便 schema 版本对上，也防御 Gson 把非空 String 塞 null 的极端场景。
     * 至少得有一个 feature，且 campus/name 非空。
     */
    private fun CampusMapGeoJson?.isValidForRender(): Boolean {
        if (this == null) return false
        if (features.isEmpty()) return false
        return features.any { feat ->
            // Kotlin 类型上非空，但 Gson 反射可能绕过；用 @Suppress 明确说明
            @Suppress("SENSELESS_COMPARISON", "USELESS_ELVIS")
            val campus: String? = feat.properties.campus
            @Suppress("SENSELESS_COMPARISON", "USELESS_ELVIS")
            val name: String? = feat.properties.name
            !campus.isNullOrBlank() && !name.isNullOrBlank()
        }
    }

    private fun cacheFile(): File? = runCatching {
        File(PlanetApplication.appContext.filesDir, CACHE_DIR).let { File(it, CACHE_FILE) }
    }.getOrNull()
}
