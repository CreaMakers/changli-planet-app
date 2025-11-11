package com.creamaker.changli_planet_app.freshNews.data.remote.api

import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.FreshNews
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.FreshNewsItem
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.FreshNewsResponse
import com.creamaker.changli_planet_app.freshNews.data.remote.dto.CommonResult
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.http.DELETE
import retrofit2.http.GET
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
    ): CommonResult<FreshNews>

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
    suspend fun addFavorite(
        @Path("user_id") userId: Int,
        @Path("news_id") freshNewsId: Int
    ): CommonResult<Boolean>

    @DELETE("/app/fresh_news/favorites/delete/{user_id}/{news_id}")
    suspend fun deleteFavorite(
        @Path("user_id") userId: Int,
        @Path("news_id") freshNewsId: Int
    ): CommonResult<Boolean>


}

fun File.toImagePart(partName: String): MultipartBody.Part = MultipartBody.Part.createFormData(
    name = partName,
    filename = this.name,
    body = this.asRequestBody("image/*".toMediaType())
)
