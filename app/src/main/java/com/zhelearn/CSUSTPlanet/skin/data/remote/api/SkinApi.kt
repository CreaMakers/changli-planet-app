package com.zhelearn.CSUSTPlanet.skin.data.remote.api

import com.zhelearn.CSUSTPlanet.freshNews.data.remote.dto.CommonResult
import com.zhelearn.CSUSTPlanet.skin.data.model.Skin
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming

interface SkinApi {
    @GET("skin/list")
    suspend fun getAllSkins(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): CommonResult<List<Skin>>

    @Streaming
    @GET("skin")
    suspend fun downloadSkinFile(
        @Query("name") name: String,
        @Query("id") id: Int
    ): Response<ResponseBody>
}