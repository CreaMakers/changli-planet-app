package com.creamaker.changli_planet_app.common.data.remote.dto

/**
 * 旧版（自研 user 服务）应用更新 DTO。
 *
 * 保留原因：新版 Go v1 `/config/app-versions/check` 接口上线期间需要一段兼容过渡，
 * 新接口失败时会自动降级到旧接口 `GET {UserIp}/apk`。
 *
 * 新接口稳定后可以整体删除本 DTO 与 [com.creamaker.changli_planet_app.common.data.remote.api.LegacyApkApi]。
 */
data class ApkInfo(
    val versionCode: Int,
    var versionName: String,
    val downloadUrl: String,
    val updateMessage: String,
    val createdAt: String
)

data class ApkResponse(
    val code: String,
    val msg: String,
    val data: ApkInfo?
)
