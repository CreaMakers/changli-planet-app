package com.example.changli_planet_app.Activity.Store

import com.example.changli_planet_app.Activity.Action.FoundThingAction
import com.example.changli_planet_app.Activity.State.FoundThingState
import com.example.changli_planet_app.Core.Store

class FoundThingStore:Store<FoundThingState,FoundThingAction>() {
    var currentState=FoundThingState()
    override fun handleEvent(action: FoundThingAction) {
        currentState=when(action){
            is FoundThingAction.initilaize -> {
                _state.onNext(currentState)
                currentState
            }
            is FoundThingAction.publish ->{
                //TODO()
                currentState
            }
            is FoundThingAction.inputText ->{
                if(action.type=="foundThingName"){
                    currentState.foundThingName=action.content
                }
                else if(action.type=="foundThingDescribe"){
                    currentState.foundThingDescribe=action.content
                }
                else if(action.type=="name"){
                    currentState.name=action.content
                }
                else if(action.type=="id"){
                    currentState.id=action.content
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
            is FoundThingAction.inputImage ->{
                currentState.foundThingPicture=action.image
                _state.onNext(currentState)
                currentState
            }
        }
    }
    private fun checkEnable():Boolean{
        return currentState.foundThingName.isNotEmpty()&&currentState.foundThingDescribe.isNotEmpty()
    }
}