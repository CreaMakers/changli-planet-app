package com.example.changli_planet_app.Network.repository

import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Resource
import com.example.changli_planet_app.Network.Response.FreshNewsItem
import com.example.changli_planet_app.Network.Response.FreshNewsResponse
import okhttp3.Response
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FreshNewsRepository {

    suspend fun getFreshNewsByTime(page: Int, pageSize: Int): Resource<List<FreshNewsItem>> =
        suspendCoroutine {
            val httpHelper = HttpUrlHelper.HttpRequest()
                .get(PlanetApplication.FreshNewsIp + "/all/by_time")
                .addQueryParam("page", page.toString())
                .addQueryParam("pageSize", pageSize.toString())
                .build()

            val response = OkHttpHelper.sendRequest(httpHelper, object : RequestCallback {
                override fun onSuccess(response: Response) {
                    val fromJson = OkHttpHelper.gson.fromJson(
                        response.body?.toString(),
                        FreshNewsResponse::class.java
                    )
                    when (fromJson.code) {
                        "200" -> {
                            val userId = fromJson.data
                        }

                        else -> {
                            it.resume(Resource.Error(fromJson.msg))
                        }
                    }
                }

                override fun onFailure(error: String) {
                    it.resume(Resource.Error(error))
                }
            })
        }
}