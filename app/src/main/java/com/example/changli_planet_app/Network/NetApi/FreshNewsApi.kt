package com.example.changli_planet_app.Network.NetApi

import android.devicelock.DeviceId
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Data.jsonbean.CommonResult
import com.example.changli_planet_app.Network.Response.FreshNews
import com.example.changli_planet_app.Network.Response.FreshNewsItem
import com.example.changli_planet_app.Network.Response.FreshNewsResponse
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.File


interface FreshNewsApi {
    @Multipart
    @POST("fresh_news")
    suspend fun postFreshNews(
        @Part images: List<MultipartBody.Part>,
        @Part("fresh_news") freshNews: RequestBody,
    ): CommonResult<FreshNewsItem>

    @GET("fresh_news/all/by_time")
    suspend fun getNewsListByTime(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
    ): FreshNewsResponse

    @POST("fresh_news/{fresh_news_id}/likes/{user_id}")
    suspend fun likeNews(
        @Path("fresh_news_id") freshNewsId: Int,
        @Path("user_id") userId: Int
    ): CommonResult<Boolean>

    @POST("/app/fresh_news/favorites/add/{user_id}/{news_id}")
    suspend fun favoriteNews(
        @Path("user_id") userId: Int,
        @Path("news_id") freshNewsId: Int
    ): CommonResult<Boolean>

}

fun File.toImagePart(partName: String): MultipartBody.Part = MultipartBody.Part.createFormData(
    name = partName,
    filename = this.name,
    body = this.asRequestBody("image/*".toMediaType())
)
