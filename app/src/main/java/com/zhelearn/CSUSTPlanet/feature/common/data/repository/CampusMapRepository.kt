package com.zhelearn.CSUSTPlanet.feature.common.data.repository

import android.util.Log
import com.zhelearn.CSUSTPlanet.core.PlanetApplication
import com.zhelearn.CSUSTPlanet.feature.common.data.remote.api.CampusMapApi
import com.zhelearn.CSUSTPlanet.feature.common.data.remote.dto.CampusMapGeoJson
import com.zhelearn.CSUSTPlanet.utils.RetrofitUtils
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
    private const val BUNDLED_MAP_ASSET = "campus_map/osm_campus_map.json"

    /** 本地缓存 schema 版本号。**改动 CampusMapDto 字段时必须同步 +1**。 */
    private const val CACHE_SCHEMA_VERSION = 3

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

    /** 读取随 APK 发布的 OSM 校园轮廓，首装、离线和接口失败时都能立即显示地图。 */
    suspend fun loadBundledMap(): CampusMapGeoJson? = withContext(Dispatchers.IO) {
        runCatching {
            PlanetApplication.appContext.assets.open(BUNDLED_MAP_ASSET).bufferedReader().use { reader ->
                gson.fromJson(reader, CampusMapGeoJson::class.java)
            }
        }.getOrNull()?.takeIf { it.isValidForRender() }
    }

    /**
     * 服务端数据优先覆盖同校区同名要素；OSM 的未命名轮廓和服务端没有的场地继续保留。
     * 这样旧接口无需立即扩 schema，也能逐步补上操场、水域和绿地。
     */
    fun mergeWithBundledMap(
        remote: CampusMapGeoJson,
        bundled: CampusMapGeoJson?,
    ): CampusMapGeoJson {
        if (bundled == null) return remote
        val remoteNamedKeys = buildSet {
            for (feature in remote.features) {
                val name = feature.properties.name.trim()
                if (name.isNotEmpty()) add(feature.properties.campus + "|" + name)
            }
        }
        val mergedFeatures = buildList {
            addAll(remote.features)
            for (feature in bundled.features) {
                val name = feature.properties.name.trim()
                val semanticKey = feature.properties.campus + "|" + name
                if (name.isEmpty() || semanticKey !in remoteNamedKeys) add(feature)
            }
        }
        val remoteRoads = remote.roads.orEmpty()
        val remoteRoadIds = buildSet {
            for (road in remoteRoads) add(road.properties.id)
        }
        val mergedRoads = buildList {
            addAll(remoteRoads)
            for (road in bundled.roads.orEmpty()) {
                if (road.properties.id !in remoteRoadIds) add(road)
            }
        }
        return CampusMapGeoJson(
            type = "FeatureCollection",
            features = mergedFeatures,
            roads = mergedRoads,
        )
    }

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
     * 至少得有一个带校区和有效 Polygon 的 feature；未命名 OSM 轮廓允许存在。
     */
    private fun CampusMapGeoJson?.isValidForRender(): Boolean {
        if (this == null) return false
        if (features.isEmpty()) return false
        return features.any { feat ->
            // Kotlin 类型上非空，但 Gson 反射可能绕过；用 @Suppress 明确说明
            @Suppress("SENSELESS_COMPARISON", "USELESS_ELVIS")
            val campus: String? = feat.properties.campus
            !campus.isNullOrBlank() && feat.geometry.coordinates.firstOrNull().orEmpty().size >= 3
        }
    }

    private fun cacheFile(): File? = runCatching {
        File(PlanetApplication.appContext.filesDir, CACHE_DIR).let { File(it, CACHE_FILE) }
    }.getOrNull()
}
