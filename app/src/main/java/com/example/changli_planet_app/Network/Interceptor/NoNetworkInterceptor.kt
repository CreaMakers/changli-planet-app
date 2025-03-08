package com.example.changli_planet_app.Network.Interceptor

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.example.changli_planet_app.Core.PlanetApplication
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class NoNetworkInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val builder = request.newBuilder()

        if (!isNetworkConnected()) {
            builder.cacheControl(CacheControl.FORCE_CACHE)
        }

        val response = chain.proceed(builder.build())

        if (!response.isSuccessful && response.code == 504) { // 504 表示没有缓存
            // 返回一个自定义的错误响应
            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("No network and no cache available")
                .body("{\"msg\": \"No network and no cache available\"}".toResponseBody())
                .build()
        }

        return chain.proceed(builder.build())
    }


    private fun isNetworkConnected(): Boolean {
        val connectivityManager = PlanetApplication.appContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0 及以上版本
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            // Android 6.0 以下版本
            @Suppress("DEPRECATION")
            connectivityManager.activeNetworkInfo?.isConnected == true
        }
    }
}