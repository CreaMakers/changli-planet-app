package com.zhelearn.CSUSTPlanet.overview.data.remote.api

import com.zhelearn.CSUSTPlanet.overview.data.remote.dto.AnnouncementDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 长理星球 Go 服务端「公告」接口。
 *
 * BaseUrl：`instancePlanet`（见 [com.zhelearn.CSUSTPlanet.utils.RetrofitUtils]）。
 */
interface AnnouncementApi {

    /**
     * 按平台获取公告列表，服务端按创建时间倒序返回。
     *
     * 示例：`GET /v1/config/announcements?platform=android`
     */
    @GET("v1/config/announcements")
    suspend fun getAnnouncements(
        @Query("platform") platform: String = "android"
    ): List<AnnouncementDto>
}
