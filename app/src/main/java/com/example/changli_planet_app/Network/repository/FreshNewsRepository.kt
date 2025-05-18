package com.example.changli_planet_app.Network.repository

import android.util.Log
import com.example.changli_planet_app.Cache.UserInfoManager
import com.example.changli_planet_app.Network.NetApi.FreshNewsApi
import com.example.changli_planet_app.Network.NetApi.toImagePart
import com.example.changli_planet_app.Network.Resource
import com.example.changli_planet_app.Network.Response.FreshNews
import com.example.changli_planet_app.Network.Response.FreshNewsItem
import com.example.changli_planet_app.Network.Response.FreshNews_Publish
import com.example.changli_planet_app.Utils.RetrofitUtils
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
        emit(Resource.Loading())
        try {
            val freshNewsResponse = service.value.getNewsListByTime(page, pageSize)

            when (freshNewsResponse.code) {
                "200" -> {
                    val freshNewsList = freshNewsResponse.data ?: emptyList()
                    emit(Resource.Success(freshNewsList))
                }

                else -> {
                    emit(Resource.Error(freshNewsResponse.msg ?: "获取新闻列表失败"))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("FreshNews", e.message.toString())
            emit(Resource.Error(e.message ?: "网络错误"))
        }
    }
    //喜欢新鲜事
    fun likeNews(freshNewsId: Int) = flow {
        emit(Resource.Loading())
        try {
            // 直接使用当前登录用户ID
            val currentUserId = UserInfoManager.userId

            val result = service.value.likeNews(freshNewsId, currentUserId)
            when {
                result.code == "200" -> {
                    emit(Resource.Success(true))
                }
                else -> {
                    emit(Resource.Error(result.msg ?: "点赞失败"))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("FreshNews", "点赞失败: ${e.message}")
            emit(Resource.Error(e.message ?: "网络错误"))
        }
    }
    //收藏新鲜事
    fun favoriteNews(freshNewsId: Int) = flow {
        emit(Resource.Loading())
        try {
            val currentUserId = UserInfoManager.userId

            val result = service.value.favoriteNews(currentUserId, freshNewsId)
            when {
                result.code == "200" -> {
                    emit(Resource.Success(true))
                }
                else -> {
                    emit(Resource.Error(result.msg ?: "收藏失败"))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("FreshNews", "收藏失败: ${e.message}")
            emit(Resource.Error(e.message ?: "网络错误"))
        }
    }


}