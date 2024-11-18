package com.example.changli_planet_app.Activity.Store

import android.util.Log
import com.example.changli_planet_app.Activity.Action.ElectronicAction
import com.example.changli_planet_app.Activity.State.ElectronicState
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Store
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import org.greenrobot.eventbus.EventBus

/**
 * 统一调度器，接受事件并进行Dispatch并处理
 * 因传统Redux的reducer和store分开过于臃肿繁琐，故结合二者，Dispatch和处理事件逻辑全都要在Store里实现
 * 状态流要使用PublishSubject
 */
class ElectronicStore:Store<ElectronicState,ElectronicAction>() {
    private var currentState = ElectronicState("选择校区","选择宿舍楼")
    //处理事件
    override fun handleEvent(action:ElectronicAction){
        currentState = when(action){
            is ElectronicAction.selectAddress->{
                currentState.address = action.address
                _state.onNext(currentState)
                currentState
            }
            is ElectronicAction.selectBuild->{
                currentState.buildId = action.buildId
                _state.onNext(currentState)
                currentState
            }
            is ElectronicAction.queryElectronic->{
                val builder = HttpUrlHelper.HttpRequest()
                    .get(PlanetApplication.ToolIp + "/dormitory-electricity")
                    .build()
                TODO()
            }
        }
    }
}