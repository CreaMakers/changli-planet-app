package com.example.changli_planet_app.Util

import com.example.changli_planet_app.Core.PlanetApplication
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitUtils {
    private const val FreshNewsIp="http://113.44.47.220:8085/app/"
    private const val UserIp="http://113.44.47.220:8083/app/users/"

    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // 或 Level.BODY
    }
    //添加公共请求头
    private val okHttpClient:OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor{chain ->
                val originRequest=chain.request()
                val newRequest=originRequest.newBuilder()
                    .header("deviceId", PlanetApplication.deviceId)
                    .header("Authorization", PlanetApplication.accessToken ?: "")
                    .build()
                chain.proceed(newRequest)
            }
            //.addInterceptor(loggingInterceptor)
            .build()
    }
    val instanceNewFresh:Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(FreshNewsIp)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val instanceUser:Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(UserIp)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}