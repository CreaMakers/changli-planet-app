package com.example.changli_planet_app.Activity.Action

import com.example.changli_planet_app.Data.jsonbean.LoseThing
import java.io.File

sealed class FoundThingAction {
    object initilaize:FoundThingAction()
    data class publish(val loseThing: LoseThing):FoundThingAction()
    data class inputText(val content:String,val type:String):FoundThingAction()
    data class inputImage(val image: File):FoundThingAction()
}