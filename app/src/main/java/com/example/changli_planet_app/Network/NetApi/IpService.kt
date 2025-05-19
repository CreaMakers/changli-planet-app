package com.example.changli_planet_app.Network.NetApi

import com.example.changli_planet_app.Data.jsonbean.CommonResult
import com.example.changli_planet_app.Network.Response.IpLocationResponse
import retrofit2.http.GET

interface IpService {
    @GET("json/")
    suspend fun getLocation(): CommonResult<IpLocationResponse>
}