package com.example.changli_planet_app.Activity.State

import android.graphics.Picture
import android.widget.ImageView
import java.io.File


data class LoseThingState(
    var loseThingName:String="",
    var loseThingDescribe:String="",
    var loseThingPicture: File?=null,
    var name:String="",
    var phone:String="",
    var isEnable:Boolean=false
)
