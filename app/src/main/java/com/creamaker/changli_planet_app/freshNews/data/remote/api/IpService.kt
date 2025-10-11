package com.creamaker.changli_planet_app.freshNews.data.remote.api

import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.IpLocationResponse
import com.creamaker.changli_planet_app.freshNews.data.remote.dto.CommonResult
import retrofit2.http.GET

interface IpService {
    @GET("json/")
    suspend fun getLocation(): CommonResult<IpLocationResponse>
}