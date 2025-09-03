package com.example.changli_planet_app.feature.lostfound.data.remote.dto

data class LoseThing(
    var user_image:Int,            //用户的头像
    var user_name:String="",             //用户的昵称
    var user_faculty:String="",          //用户的学院
    var lose_title:String="",            //丢失帖子的主题，一般为物品名称
    var lose_content:String="",          //丢失帖子的正文
    var lose_thing_picture: Int?=null     //丢失物品的图片
)