package com.example.changli_planet_app.Activity.State

import java.io.File

data class FoundThingState(
    var foundThingName:String="",
    var foundThingDescribe:String="",
    var foundThingPicture: File?=null,
    var name:String="",
    var id:String="",
    var isEnable:Boolean=false
)
