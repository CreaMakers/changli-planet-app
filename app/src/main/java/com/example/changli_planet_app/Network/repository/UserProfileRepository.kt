package com.example.changli_planet_app.Network.repository

import com.example.changli_planet_app.Network.NetApi.UserProfileApi
import com.example.changli_planet_app.Util.RetrofitUtils
import kotlinx.coroutines.flow.flow
import okio.EOFException
import okio.IOException

class UserProfileRepository private constructor() {
    companion object{
        val instance by lazy {  UserProfileRepository()}
    }
    private var maxRetries=3
    private val service by lazy { RetrofitUtils.instanceUser.create(UserProfileApi::class.java) }

    fun getUserInformationById(userId:Int)= flow {
        var attempt=0
        while (attempt<maxRetries){
            try {
                emit(service.getUserInformationById(userId))
                break
            }catch (e:IOException){
                if(e is EOFException){
                    attempt++
                    kotlinx.coroutines.delay(1000L*attempt)
                }
            }
        }

    }
}