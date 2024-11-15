package com.example.changli_planet_app.Activity.State

/**
 * 每个页面都应有数据类来管理数据，数据类是不可变的
 */
data class ElectronicState(
    val address:String,
    val buildId:String,
    val nod:String
)
