package com.example.changli_planet_app.Activity.Action

import com.example.changli_planet_app.Data.jsonbean.LoseThing
import java.io.File

sealed class LoseThingAction {
    object initilaize:LoseThingAction()
    data class publish(val loseThing: LoseThing):LoseThingAction()
    data class inputText(val content:String,val type:String):LoseThingAction()
    data class inputImage(val image:File):LoseThingAction()
}