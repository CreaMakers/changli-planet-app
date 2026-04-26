package com.creamaker.changli_planet_app.feature.common.data.repository

import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.feature.common.data.remote.api.CampusMapApi
import com.creamaker.changli_planet_app.feature.common.data.remote.dto.CampusMapGeoJson
import com.creamaker.changli_planet_app.utils.RetrofitUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 校园地图数据仓库：负责网络拉取 + 本地 JSON 缓存。
 *
 * - 本地缓存目录：`filesDir/campus_map/map.json`，与 iOS 的 `cachesDirectory/map.json` 语义一致。
 * - 网络拉取使用 [RetrofitUtils.instancePlanet]（和校历等 config 接口共用）。
 * - 所有 IO 调用都在 [Dispatchers.IO] 执行，主线程安全。
 */
object CampusMapRepository {

    private const val CACHE_DIR = "campus_map"
    private const val CACHE_FILE = "map.json"

    private val api: CampusMapApi by lazy {
        RetrofitUtils.instancePlanet.create(CampusMapApi::class.java)
    }

    private val gson: Gson by lazy { Gson() }

    /** 读本地缓存；读失败/不存在时返回 null（不抛异常）。 */
    suspend fun loadCache(): CampusMapGeoJson? = withContext(Dispatchers.IO) {
        runCatching {
            val file = cacheFile() ?: return@runCatching null
            if (!file.exists() || file.length() == 0L) return@runCatching null
            gson.fromJson(file.readText(Charsets.UTF_8), CampusMapGeoJson::class.java)
        }.getOrNull()
    }

    /** 拉取线上最新数据，并写入本地缓存。异常原样抛出供上层做 UI 反馈。 */
    suspend fun fetchRemoteAndCache(): CampusMapGeoJson = withContext(Dispatchers.IO) {
        val geoJson = api.getCampusMap()
        runCatching {
            cacheFile()?.apply {
                parentFile?.mkdirs()
                writeText(gson.toJson(geoJson), Charsets.UTF_8)
            }
        } // 缓存写入失败不影响主流程
        geoJson
    }

    private fun cacheFile(): File? = runCatching {
        File(PlanetApplication.appContext.filesDir, CACHE_DIR).let { File(it, CACHE_FILE) }
    }.getOrNull()
}
