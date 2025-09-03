package com.example.changli_planet_app.feature.lostfound.redux.action

import com.example.changli_planet_app.feature.lostfound.data.remote.dto.LoseThing
import java.io.File

/**
 * 失误招领
 */
sealed class LoseThingAction {
    object initilaize:LoseThingAction()
    data class publish(val loseThing: LoseThing):LoseThingAction()
    data class inputText(val content:String,val type:String):LoseThingAction()
    data class inputImage(val image: File):LoseThingAction()
}