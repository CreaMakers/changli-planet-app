package com.creamaker.changli_planet_app.freshNews.data.remote.api


import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level1Comments
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level2Comments
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.LevelCommentRequest
import com.creamaker.changli_planet_app.freshNews.data.remote.dto.CommonResult
import com.google.gson.annotations.SerializedName
import com.tencent.mmkv.MMKV.pageSize
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CommentsApi {
    @POST("comments/fresh_news/add")
    suspend fun addLevel1Comment(@Body body: LevelCommentRequest): CommonResult<Int>


    @POST("comments/replies/add")
    suspend fun addLevel2Comment(@Body body: LevelCommentRequest): CommonResult<Int>
    @GET("comments/{fresh_news_id}/list")
    suspend fun getLevel1Comments(
        @Path("fresh_news_id") freshNewsId: Int,
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): CommonResult<Level1Comments>
    @GET("comments/{fresh_news_id}/replies")
    suspend fun getLevel2Comments(
        @Path("fresh_news_id") freshNewsId: Int,
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
        @Query("comment_id") parentId: Int,
    ): CommonResult<Level2Comments>
    @POST("comments/{comment_id}/likes/{user_id}/{is_Parent}")
    suspend fun likeComment(
        @Path("comment_id") commentId: Int,
        @Path("user_id") userId: Int,
        @Path("is_Parent") isParent: Int
    ): CommonResult<Any>
    @POST("refresh_like_count")
    suspend fun refreshLikeCount(): CommonResult<Any>
}