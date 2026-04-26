package com.creamaker.changli_planet_app.feature.common.map

/**
 * 长沙理工大学两个校区的枚举与常量定义。
 *
 * GeoJSON 里 `properties.campus` 的取值是中文字符串（`"金盆岭"` / `"云塘"`），
 * 本枚举通过 [rawName] 与之匹配，避免在 UI/过滤处散落硬编码字符串。
 */
enum class Campus(val rawName: String, val displayName: String, val centerLat: Double, val centerLon: Double) {
    // 中心坐标 = GeoJSON 中该校区所有 polygon 的 bbox 中心（WGS-84，UI 层再转 GCJ-02）。
    // 之前使用 iOS 直接复制的标注点，导致整个校区 polygon 集群偏向屏幕左上角（相机 target 落在
    // 集群东南方 ~500m 处）。这里按真实几何 bbox 重算，定位即可落在校区正中。
    JINPENLING("金盆岭", "金盆岭校区", 28.159333, 112.971979),
    YUNTANG("云塘", "云塘校区", 28.071200, 113.002808);

    companion object {
        fun fromRawName(raw: String?): Campus? = entries.firstOrNull { it.rawName == raw }
    }
}
