package com.creamaker.changli_planet_app.feature.common.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * 校园地图 GeoJSON 响应体，对应 `GET {PlanetIp}/config/campus-map`。
 *
 * 后端直接返回标准 GeoJSON `FeatureCollection`：
 * ```json
 * {
 *   "type": "FeatureCollection",
 *   "features": [ { "type":"Feature", "properties":{...}, "geometry":{...} } ]
 * }
 * ```
 * 所有坐标为 **WGS-84**（经度, 纬度），渲染到高德地图时需先转成 GCJ-02。
 */
@Keep
data class CampusMapGeoJson(
    @SerializedName("type") val type: String,
    @SerializedName("features") val features: List<CampusMapFeature> = emptyList()
)

/**
 * 单个建筑要素。
 *
 * [stableId] 用于 Compose `LazyColumn` / Polygon Map 的唯一键，语义与 iOS 一致
 * （`name + campus`），避免用对象哈希做 key 导致列表滚动时无故重建。
 */
@Keep
data class CampusMapFeature(
    @SerializedName("type") val type: String,
    @SerializedName("properties") val properties: CampusMapFeatureProperties,
    @SerializedName("geometry") val geometry: CampusMapFeatureGeometry
) {
    val stableId: String get() = properties.name + "|" + properties.campus
}

@Keep
data class CampusMapFeatureProperties(
    @SerializedName("name") val name: String,
    @SerializedName("campus") val campus: String,
    @SerializedName("category") val category: String
)

/**
 * 几何信息。目前后端只会下发 `Polygon`，`coordinates` 层级为
 * `[[[lon, lat], ...]]`（外环 + 可选内环，单环情况下只取第一环）。
 */
@Keep
data class CampusMapFeatureGeometry(
    @SerializedName("type") val type: String,
    @SerializedName("coordinates") val coordinates: List<List<List<Double>>> = emptyList()
)
