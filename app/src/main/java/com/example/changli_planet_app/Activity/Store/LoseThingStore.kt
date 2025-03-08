package com.example.changli_planet_app.Activity.Store

import com.example.changli_planet_app.Activity.Action.LoseThingAction
import com.example.changli_planet_app.Activity.State.LoseThingState
import com.example.changli_planet_app.Core.Store
import okhttp3.internal.notifyAll

class LoseThingStore:Store<LoseThingState,LoseThingAction>() {
    var currentState=LoseThingState()
    override fun handleEvent(action: LoseThingAction) {
        currentState=when(action){
            is LoseThingAction.initilaize->{
                _state.onNext(currentState)
                currentState
            }
            is LoseThingAction.publish->{
                //TODO()
                currentState
            }
            is LoseThingAction.inputText->{
                if(action.type=="loseThingName"){
                    currentState.loseThingName=action.content
                }
                else if(action.type=="loseThingDescribe"){
                    currentState.loseThingDescribe=action.content
                }
                else if(action.type=="name"){
                    currentState.name=action.content
                }
                else if(action.type=="phone"){
                    currentState.phone=action.content
                }
                if(!currentState.isEnable&&checkEnable()){
                    currentState.isEnable=true
                }
                if(!checkEnable()){
                    currentState.isEnable=false
                }
                _state.onNext(currentState)
                currentState
            }
            is LoseThingAction.inputImage->{
                currentState.loseThingPicture=action.image
                _state.onNext(currentState)
                currentState
            }
        }
    }
    private fun checkEnable():Boolean{
        return currentState.loseThingName.isNotEmpty()&&currentState.name.isNotEmpty()&&currentState.phone.isNotEmpty()
    }
}