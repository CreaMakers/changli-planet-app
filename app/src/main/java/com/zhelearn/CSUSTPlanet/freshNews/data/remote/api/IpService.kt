package com.zhelearn.CSUSTPlanet.freshNews.data.remote.api

import com.zhelearn.CSUSTPlanet.freshNews.data.local.mmkv.model.IpLocationResponse
import com.zhelearn.CSUSTPlanet.freshNews.data.remote.dto.CommonResult
import retrofit2.http.GET
import retrofit2.http.Query

interface IpService {
    @GET("json/")
    suspend fun getLocation(@Query("lang") lang: String = "zh-CN"): IpLocationResponse
}