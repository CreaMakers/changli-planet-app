package com.example.changli_planet_app.Network.repository

import android.util.Log
import com.example.changli_planet_app.Network.NetApi.FreshNewsApi
import com.example.changli_planet_app.Network.NetApi.toImagePart
import com.example.changli_planet_app.Network.Resource
import com.example.changli_planet_app.Network.Response.FreshNewsItem
import com.example.changli_planet_app.Network.Response.FreshNews_Publish
import com.example.changli_planet_app.Util.RetrofitUtils
import com.google.gson.Gson
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


class FreshNewsRepository private constructor() {
    companion object {
        val instance by lazy { FreshNewsRepository() }
    }

    private val service = lazy { RetrofitUtils.instanceNewFresh.create(FreshNewsApi::class.java) }

    fun postFreshNews(images: List<File>, freshNews: FreshNews_Publish) = flow {
        Log.e("FreshNewsRepository", "enter flow")
        try {
            val imagesPart = if (images.isNotEmpty()) {
                images.map { it.toImagePart("images") }
            } else {
                listOf(
                    MultipartBody.Part.createFormData(
                        "images",
                        "",
                        ByteArray(0).toRequestBody("application/octet-stream".toMediaType())
                    )
                )
            }
            val FreshNewsBody =
                Gson().toJson(freshNews).toRequestBody("application/json".toMediaType())
            emit(service.value.postFreshNews(imagesPart, FreshNewsBody))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getNewsListByTime(page: Int, pageSize: Int) = flow {
        val freshNewsResponse = service.value.getNewsListByTime(page, pageSize)
        when (freshNewsResponse.code) {
            "200" -> {
                val freshNewsList = freshNewsResponse.data
                freshNewsList?.forEach { freshNews ->
                    emit(freshNews)
                }
            }

            else -> {
                emit(Resource.Error<FreshNewsItem>(freshNewsResponse.msg))
            }
        }
    }
}