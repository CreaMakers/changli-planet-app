package com.example.changli_planet_app.feature.mooc.data.remote.api

import com.example.changli_planet_app.feature.mooc.data.remote.dto.CheckCaptchaResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SSOAuthApi {
    @GET("/authserver/checkNeedCaptcha.htl")
    suspend fun checkNeedCaptcha(
        @Query("username") username: String,
        @Query("_") timestamp: Long
    ): Response<CheckCaptchaResponse>

    @GET("/authserver/login")
    suspend fun getLoginForm(
        @Query("service") service: String = "https://ehall.csust.edu.cn/login"
    ): Response<String>

    @FormUrlEncoded
    @POST("/authserver/login")
    suspend fun login(
        @Query("service") service: String = "https://ehall.csust.edu.cn/login",
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("captcha") captcha: String = "",
        @Field("_eventId") eventId: String = "submit",
        @Field("cllt") cllt: String = "userNameLogin",
        @Field("dllt") dllt: String = "generalLogin",
        @Field("lt") lt: String = "",
        @Field("execution") execution: String
    ): Response<String>

    @GET("/authserver/logout")
    suspend fun logoutAuthserver(): Response<String>

    @GET("/authserver/getCaptcha.htl")
    suspend fun getCaptcha(): Response<ResponseBody>  // 验证码返回图片数据
}