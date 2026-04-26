package com.creamaker.changli_planet_app.common.data.remote.api

import com.creamaker.changli_planet_app.common.data.remote.dto.ApkResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 旧版应用更新接口（自研 user 服务）。
 *
 * `https://user.csust.creamaker.cn/`。
 * 仅在新版 Go v1 `/config/app-versions/check` 接口失败时作为兜底调用，
 * 覆盖灰度 / 后端发布窗口期。新接口稳定后可整体删除。
 */
interface LegacyApkApi {

    @GET("apk")
    suspend fun queryLatestApk(
        @Query("versionCode") versionCode: String,
        @Query("versionName") versionName: String
    ): ApkResponse
}
