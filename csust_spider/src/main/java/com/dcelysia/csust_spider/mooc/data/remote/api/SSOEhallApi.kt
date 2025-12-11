package com.dcelysia.csust_spider.mooc.data.remote.api

import com.dcelysia.csust_spider.mooc.data.remote.dto.LoginUserResponse
import retrofit2.Response
import retrofit2.http.GET

interface SSOEhallApi {
    @GET("/getLoginUser")
    suspend fun getLoginUser(): Response<LoginUserResponse>

    @GET("/logout")
    suspend fun logoutEhall(): Response<String>
}