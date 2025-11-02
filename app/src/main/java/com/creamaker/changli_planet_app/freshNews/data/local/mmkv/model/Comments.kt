package com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model

import com.google.gson.annotations.SerializedName

data class Level1Comments(
    val freshNewsId:Int,
    //一级评论数量
    val firstCommentCount:Int,
    val isActive:Int,
    val commentsList: List<Level1Comment>
){
    data class Level1Comment(
        val commentId: Int,
        // 所属新鲜事ID
        val freshNewsId: Int,
        // 点赞数
        val liked:Int,
        // 评论ID
        val content: String,
        val userId: Int,
        var userName: String,
        var userAvatar: String,
        val commentIp: String,
        // 评论内容
        // 创建时间
        val createTime: String,
        // 是否有效
        val isActive: Int,
    )
}
data class Level2Comments(
    val list: List<Level2Comment>
){
    data class Level2Comment(
        val commentId: Int,
        // 所属新鲜事ID
        val freshNewsId: Int,
        // 所属一级评论ID
        val parentCommentId: Int,
        // 点赞数
        val liked:Int,
        // 评论ID
        val content: String,
        val userId: Int,
        var userName: String,
        var userAvatar: String,
        val commentIp: String,
        // 评论内容
        // 创建时间
        val createTime: String,
        // 是否有效
        val isActive: Int,
    )
}

data class LevelCommentRequest(
    @SerializedName("fresh_news_id") val freshNewsId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("content") val content: String,
    @SerializedName("parent_comment_id") val parentCommentId: Int = 0,
    @SerializedName("comment_ip") val commentIp: String
)







