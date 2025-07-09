package com.example.changli_planet_app.Network.repository

import android.util.Log
import com.example.changli_planet_app.Cache.Room.database.UserDataBase
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Network.NetApi.UserProfileApi
import com.example.changli_planet_app.Network.Resource
import com.example.changli_planet_app.Utils.RetrofitUtils
import com.example.changli_planet_app.Utils.toEntity
import com.example.changli_planet_app.Utils.toProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class UserProfileRepository private constructor() {
    companion object {
        val instance by lazy { UserProfileRepository() }
        val cache by lazy { UserDataBase.getInstance(PlanetApplication.appContext).itemDao() }

        // 缓存时间暂时为3小时
        private const val CACHE_DURATION = 14 * 60 * 1000L
    }
    private val TAG = javaClass.simpleName

    private val service by lazy { RetrofitUtils.instanceUser.create(UserProfileApi::class.java) }

    fun getUserInformationById(userId: Int) = flow {
        val cacheUser = withContext(Dispatchers.IO) {
            cache.getUserById(userId)
        }

        if (cacheUser != null && System.currentTimeMillis() - cacheUser.cacheTime <= CACHE_DURATION) {
            emit(Resource.Success(cacheUser.toProfile()))
            return@flow
        }
        emit(Resource.Loading())
        val result = service.getUserInformationById(userId)
        if (result.code == "200" && result.data != null) {
            val userEntity = result.data.toEntity()
            cache.insertUser(userEntity)
            emit(Resource.Success(result.data))
        } else {
            emit(Resource.Error(result.msg))

            if (cacheUser != null) {
                emit(Resource.Success(cacheUser.toProfile()))
            }
        }
    }.catch { e ->
        e.printStackTrace()
        Log.e(TAG, "通过网络请求或缓存用户id获取信息出错: ${e.message}")
        emit(Resource.Error("网络错误"))
    }

    fun getUserInFormationNoCache(userId: Int) = flow {
        emit(Resource.Loading())
        val result = service.getUserInformationById(userId)
        if (result.code == "200" && result.data != null) {
            val userEntity = result.data.toEntity()
            withContext(Dispatchers.IO) {
                cache.insertUser(userEntity)
            }
            emit(Resource.Success(result.data))
        } else {
            emit(Resource.Error(result.msg))
        }
    }.catch { e ->
        e.printStackTrace()
        Log.e(TAG, "通过网络请求用户id获取信息出错: ${e.message}")
        emit(Resource.Error("网络错误"))
    }

}