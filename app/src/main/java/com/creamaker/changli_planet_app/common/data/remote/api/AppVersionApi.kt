package com.creamaker.changli_planet_app.common.data.remote.api

import com.creamaker.changli_planet_app.common.data.remote.dto.AppVersionCheckResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 长理星球 Go 服务端（v1）- 应用版本相关接口。
 *
 * 通过 [com.creamaker.changli_planet_app.utils.RetrofitUtils.instancePlanet] 创建，
 * BaseUrl 根据构建类型自动切换（debug 使用 api-dev，release 使用 api）。
 */
interface AppVersionApi {

    /**
     * 检查当前版本是否需要更新。
     *
     * 示例：
     *  - `GET /config/app-versions/check?platform=android&currentVersionCode=26`
     */
    @GET("config/app-versions/check")
    suspend fun checkUpdate(
        @Query("platform") platform: String = "android",
        @Query("currentVersionCode") currentVersionCode: Long
    ): AppVersionCheckResponse
}
