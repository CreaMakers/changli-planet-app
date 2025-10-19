package com.creamaker.changli_planet_app.freshNews.data.remote.repository

import android.util.Log
import com.creamaker.changli_planet_app.common.data.local.room.database.UserDataBase
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.network.ApiResponse
import com.creamaker.changli_planet_app.freshNews.data.remote.api.UserProfileApi
import com.creamaker.changli_planet_app.utils.RetrofitUtils
import com.creamaker.changli_planet_app.utils.toEntity
import com.creamaker.changli_planet_app.utils.toProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class UserProfileRepository private constructor() {
    companion object {
        val instance by lazy { UserProfileRepository() }
        val cache by lazy { UserDataBase.Companion.getInstance(PlanetApplication.Companion.appContext).itemDao() }

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
            emit(ApiResponse.Success(cacheUser.toProfile()))
            return@flow
        }
        emit(ApiResponse.Loading())
        val result = service.getUserInformationById(userId)
        if (result.code == "200" && result.data != null) {
            val userEntity = result.data.toEntity()
            cache.insertUser(userEntity)
            emit(ApiResponse.Success(result.data))
        } else {
            emit(ApiResponse.Error(result.msg))

            if (cacheUser != null) {
                emit(ApiResponse.Success(cacheUser.toProfile()))
            }
        }
    }.catch { e ->
        e.printStackTrace()
        Log.e(TAG, "通过网络请求或缓存用户id获取信息出错: ${e.message}")
        emit(ApiResponse.Error("网络错误"))
    }

    fun getUserInFormationNoCache(userId: Int) = flow {
        emit(ApiResponse.Loading())
        val result = service.getUserInformationById(userId)
        if (result.code == "200" && result.data != null) {
            val userEntity = result.data.toEntity()
            withContext(Dispatchers.IO) {
                cache.insertUser(userEntity)
            }
            emit(ApiResponse.Success(result.data))
        } else {
            emit(ApiResponse.Error(result.msg))
        }
    }.catch { e ->
        e.printStackTrace()
        Log.e(TAG, "通过网络请求用户id获取信息出错: ${e.message}")
        emit(ApiResponse.Error("网络错误"))
    }

}