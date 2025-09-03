package com.example.changli_planet_app.Feature.lostfound.action

import com.example.changli_planet_app.Data.jsonbean.LoseThing
import java.io.File

/**
 * 失误招领action
 */
sealed class FoundThingAction {
    object initilaize:FoundThingAction()
    data class publish(val loseThing: LoseThing):FoundThingAction()
    data class inputText(val content:String,val type:String):FoundThingAction()
    data class inputImage(val image: File):FoundThingAction()
}