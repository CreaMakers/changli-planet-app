package com.example.changli_planet_app.feature.lostfound.ui.adapter.model

data class FoundThing (
    var user_image:Int,            //用户的头像
    var user_name:String="",             //用户的昵称
    var user_faculty:String="",          //用户的学院
    var found_title:String="",            //招领帖子的主题，一般为物品名称
    var found_content:String="",          //招领帖子的正文
    var found_thing_picture: Int?=null     //招领物品的图片
)