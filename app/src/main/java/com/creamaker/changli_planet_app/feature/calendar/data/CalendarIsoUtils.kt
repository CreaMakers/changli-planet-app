package com.creamaker.changli_planet_app.feature.calendar.data

/**
 * 校历 ISO 日期工具。
 *
 * 服务端返回的时间字段（`semesterStart` / `calendarStart` 等）都是 ISO 8601 UTC 字符串，
 * 例如 `"2026-03-09T00:00:00Z"`；本工具统一负责从中抽取"本地 00:00:00"形式，
 * 用于与内置 [com.creamaker.changli_planet_app.common.cache.CommonInfo.termMap] 保持一致的存储格式。
 */
internal object CalendarIsoUtils {

    /**
     * 将 ISO 8601 字符串转为 `"yyyy-MM-dd HH:mm:ss"` 格式（时间部分固定为 `00:00:00`）。
     *
     * @param iso 例如 `"2026-03-09T00:00:00Z"` 或 `"2026-03-09"`
     * @return 例如 `"2026-03-09 00:00:00"`，无法解析时返回 null
     */
    fun isoToTermStartDate(iso: String): String? {
        if (iso.isBlank()) return null
        val datePart = iso.substringBefore('T').takeIf { it.length == 10 } ?: return null
        return "$datePart 00:00:00"
    }
}
