package com.example.changli_planet_app.Activity.Store

import com.example.changli_planet_app.Activity.Action.ElectronicAction
import com.example.changli_planet_app.Activity.State.ElectronicState
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.PlanetApplication
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 统一调度器，接受事件并进行Dispatch并处理
 * 因传统Redux的reducer和store分开过于臃肿繁琐，故结合二者，Dispatch和处理事件逻辑全都要在Store里实现
 * 状态流要使用BehaviorSubject
 */
class ElectronicStore {
    //事件流
    private val eventStream = PublishSubject.create<ElectronicAction>()
    //状态流
    private val _state = BehaviorSubject.create<ElectronicState>()
    init {
        EventBus.getDefault().register(this)
        //监听事件
        eventStream.observeOn(Schedulers.io())
                    .subscribe{action->
                        handleEvent(action)
                    }
    }
    //处理事件
    private fun handleEvent(action:ElectronicAction){
        when(action){
            is ElectronicAction.selectAddress->{
                _state.onNext(_state.value?.copy(action.address))
            }
            is ElectronicAction.selectBuild->{
                _state.onNext(_state.value?.copy(action.buildId))
            }
            is ElectronicAction.selectNod->{
                _state.onNext(_state.value?.copy(action.nod))
            }
            is ElectronicAction.queryElectronic->{
                val builder = HttpUrlHelper.HttpRequest()
                    .get(PlanetApplication.ToolIp + "dormitory-electricity")
                    .build()
                TODO()
            }
        }
    }
    /**
     * Dispatch接收到的事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun dispatch(action: ElectronicAction){
        eventStream.onNext(action)
    }
    fun state(): Observable<ElectronicState> = _state.observeOn(AndroidSchedulers.mainThread())
    /**
     * 因为无法感知生命周期，需要手动解注册
     */
    fun unRegister(){
        EventBus.getDefault().unregister(this)
    }
}