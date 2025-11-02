package com.creamaker.changli_planet_app.freshNews.data.remote.repository

import android.util.Log
import com.creamaker.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.creamaker.changli_planet_app.core.network.ApiResponse
import com.creamaker.changli_planet_app.freshNews.data.remote.api.FreshNewsApi
import com.creamaker.changli_planet_app.freshNews.data.remote.api.toImagePart
import com.creamaker.changli_planet_app.freshNews.data.remote.api.IpService
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.FreshNewsPublish
import com.creamaker.changli_planet_app.utils.RetrofitUtils
import com.google.gson.Gson
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class FreshNewsRepository private constructor() {
    companion object {
        val instance by lazy { FreshNewsRepository() }
    }
    private val TAG = "FreshNewsRepository"
    private val service = lazy { RetrofitUtils.instanceNewFresh.create(FreshNewsApi::class.java) }
    private val ipService by lazy {
        RetrofitUtils.instanceIP.create(IpService::class.java)
    }

    fun postFreshNews(images: List<File>, freshNews: FreshNewsPublish) = flow {
        Log.e("FreshNewsRepository", "enter flow")


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

    }.catch { e->
        Log.d(TAG,"发布新鲜事错误:${e}")

    }
    fun getIp(freshNews: FreshNewsPublish) = flow {
        try {
            val ipResponse = ipService.getLocation()
            if (ipResponse.status == "success") {
                val city = ipResponse.regionName
                if (city != null) {
                    freshNews.address = city
                }
                Log.d("FreshNewsRepository", "发布城市: $city")
                emit("success")
            } else {
                Log.d("FreshNewsRepository", "response:$ipResponse")
                emit("failed")
                freshNews.address = "未知"
            }
        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    fun getNewsListByTime(page: Int, pageSize: Int) = flow {
        emit(ApiResponse.Loading())
        try {
            val freshNewsResponse = service.value.getNewsListByTime(page, pageSize)

            when (freshNewsResponse.code) {
                "200" -> {
                    val freshNewsList = freshNewsResponse.data ?: emptyList()
                    emit(ApiResponse.Success(freshNewsList))
                }

                else -> {
                    emit(ApiResponse.Error(freshNewsResponse.msg))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("FreshNews", e.message.toString())
            emit(ApiResponse.Error("网络错误"))
        }
    }
    //喜欢新鲜事
    fun likeNews(freshNewsId: Int) = flow {
        emit(ApiResponse.Loading())
        try {
            val currentUserId = UserInfoManager.userId

            val result = service.value.likeNews(freshNewsId, currentUserId)
            when {
                result.code == "200" -> {
                    emit(ApiResponse.Success(true))
                }

                else -> {
                    emit(ApiResponse.Error(result.msg))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("FreshNews", "点赞失败: ${e.message}")
            emit(ApiResponse.Error("网络错误"))
        }
    }
    //收藏新鲜事
    fun favoriteNews(freshNewsId: Int, isFavoriting: Boolean) = flow {
        emit(ApiResponse.Loading())
        try {
            val currentUserId = UserInfoManager.userId
            // 根据收藏状态来调用不同的网络请求
            val result = if (isFavoriting) {
                service.value.addFavorite(currentUserId, freshNewsId)
            } else {
                service.value.deleteFavorite(currentUserId, freshNewsId)
            }

            if (result.code == "200") {
                emit(ApiResponse.Success(true))
            } else {
                emit(ApiResponse.Error(result.msg ?: "操作失败"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("FreshNews", "收藏操作失败: ${e.message}")
            emit(ApiResponse.Error("网络错误"))
        }
    }



}