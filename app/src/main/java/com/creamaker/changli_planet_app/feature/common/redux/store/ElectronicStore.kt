package com.creamaker.changli_planet_app.feature.common.redux.store

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.Store
import com.creamaker.changli_planet_app.core.network.HttpUrlHelper
import com.creamaker.changli_planet_app.core.network.MyResponse
import com.creamaker.changli_planet_app.core.network.OkHttpHelper
import com.creamaker.changli_planet_app.core.network.listener.RequestCallback
import com.creamaker.changli_planet_app.feature.common.redux.action.ElectronicAction
import com.creamaker.changli_planet_app.feature.common.redux.state.ElectronicState
import com.creamaker.changli_planet_app.widget.Dialog.NormalResponseDialog
import com.example.csustdataget.CampusCard.CampusCardHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Response

/**
 * 统一调度器，接受事件并进行Dispatch并处理
 * 因传统Redux的reducer和store分开过于臃肿繁琐，故结合二者，Dispatch和处理事件逻辑全都要在Store里实现
 * 状态流要使用PublishSubject
 */
class ElectronicStore(contexts: Activity):Store<ElectronicState,ElectronicAction>() {
    private val TAG = "ElectronicStore"
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
                CoroutineScope(Dispatchers.IO).launch {
                    val eleResponse = CampusCardHelper
                        .queryElectricity(
                            action.checkElectricity.address,
                            action.checkElectricity.buildId,
                            action.checkElectricity.nod
                        )
                    if (eleResponse == null){
                        currentState.elec = "无数据"
                        currentState.isElec = true
                        _state.onNext(currentState)
                    }
                    else{
                        Log.d(TAG,eleResponse.toString())
                        currentState.elec = eleResponse.toString()
                        currentState.isElec = true
                        _state.onNext(currentState)
                    }
                }
                //旧实现
//                Log.d("ElectronicStore","received")
//                val builder = HttpUrlHelper.HttpRequest()
//                    .header("Authorization","${PlanetApplication.accessToken}")
//                    .get(PlanetApplication.ToolIp + "/dormitory-electricity")
//                    .addQueryParam("address","${action.checkElectricity.address}")
//                    .addQueryParam("buildId","${action.checkElectricity.buildId}")
//                    .addQueryParam("nod","${action.checkElectricity.nod}")
//                    .build()
//                OkHttpHelper.sendRequest(builder,object :RequestCallback{
//                    override fun onSuccess(response: Response) {
//                        var fromJson = OkHttpHelper.gson.fromJson(
//                            response.body?.string(),
//                            MyResponse::class.java
//                        )
//
//                        currentState.elec = fromJson.msg
//                        Log.d(TAG,fromJson.msg)
//                        currentState.isElec = true
//                        _state.onNext(currentState)
//
//                    }
//                    override fun onFailure(error: String) {
//                    }
//                })
                currentState
            }
        }
    }
}