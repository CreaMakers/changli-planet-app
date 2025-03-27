package com.example.changli_planet_app.Util

import androidx.room.util.EMPTY_STRING_ARRAY
import com.example.changli_planet_app.Network.Response.FreshNews
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitUtils {
    private const val FreshNewsIp="http://113.44.47.220:8085/app/"

    val instance:Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(FreshNewsIp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}