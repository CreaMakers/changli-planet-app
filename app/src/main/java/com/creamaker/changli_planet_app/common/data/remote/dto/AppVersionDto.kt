package com.creamaker.changli_planet_app.common.data.remote.dto

/**
 * 长理星球 Go 服务端应用版本信息。
 *
 * 对应接口：`GET {PlanetIp}/config/app-versions/check?platform=android&currentVersionCode=...`
 *
 * 返回示例：
 * ```json
 * {
 *   "hasUpdate": true,
 *   "isForceUpdate": true,
 *   "latestVersion": {
 *     "createdAt": "string",
 *     "downloadUrl": "string",
 *     "isForceUpdate": true,
 *     "platform": "string",
 *     "releaseNotes": "string",
 *     "versionCode": 0,
 *     "versionName": "string"
 *   }
 * }
 * ```
 */
data class AppVersionCheckResponse(
    val hasUpdate: Boolean = false,
    val isForceUpdate: Boolean = false,
    val latestVersion: AppVersionInfo? = null
)

data class AppVersionInfo(
    val createdAt: String? = null,
    val downloadUrl: String = "",
    val isForceUpdate: Boolean = false,
    val platform: String = "",
    val releaseNotes: String = "",
    val versionCode: Int = 0,
    val versionName: String = ""
)
