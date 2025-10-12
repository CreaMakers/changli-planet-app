package com.creamaker.changli_planet_app.feature.lostfound.redux.store

import com.creamaker.changli_planet_app.core.Store
import com.creamaker.changli_planet_app.feature.lostfound.redux.action.FoundThingAction
import com.creamaker.changli_planet_app.feature.lostfound.redux.state.FoundThingState

class FoundThingStore: Store<FoundThingState, FoundThingAction>() {
    var currentState= FoundThingState()
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