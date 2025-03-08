package com.example.changli_planet_app.Activity.Store

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewStub
import android.widget.TextView
import com.airbnb.lottie.animation.content.Content
import com.example.changli_planet_app.Activity.Action.ElectronicAction
import com.example.changli_planet_app.Activity.State.ElectronicState
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Store
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.MyResponse
import com.example.changli_planet_app.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import okhttp3.Response
import okhttp3.internal.http2.Http2Reader
import org.greenrobot.eventbus.EventBus

/**
 * 统一调度器，接受事件并进行Dispatch并处理
 * 因传统Redux的reducer和store分开过于臃肿繁琐，故结合二者，Dispatch和处理事件逻辑全都要在Store里实现
 * 状态流要使用PublishSubject
 */
class ElectronicStore(contexts: Activity):Store<ElectronicState,ElectronicAction>() {
    private var currentState = ElectronicState("选择校区","选择宿舍楼")
    val content= contexts;
    var handler = Handler(Looper.getMainLooper())
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
                    .header("Authorization","${PlanetApplication.accessToken}")
                    .get(PlanetApplication.ToolIp + "/dormitory-electricity")
                    .addQueryParam("address","${action.checkElectricity.address}")
                    .addQueryParam("buildId","${action.checkElectricity.buildId}")
                    .addQueryParam("nod","${action.checkElectricity.nod}")
                    .build()
                OkHttpHelper.sendRequest(builder,object :RequestCallback{
                    override fun onSuccess(response: Response) {
                        var fromJson = OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            MyResponse::class.java
                        )
                        if(!currentState.isElec){
                            currentState.elec = fromJson.msg
                            currentState.isElec = true
                            _state.onNext(currentState)
                        }
                    }
                    override fun onFailure(error: String) {
                    }
                })
                currentState
            }
        }
    }
}