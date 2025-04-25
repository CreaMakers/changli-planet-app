package com.example.changli_planet_app.Network.repository

import com.example.changli_planet_app.Cache.Room.database.UserDataBase
import com.example.changli_planet_app.Cache.Room.entity.UserEntity
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Data.jsonbean.CommonResult
import com.example.changli_planet_app.Network.NetApi.UserProfileApi
import com.example.changli_planet_app.Network.Resource
import com.example.changli_planet_app.Network.Response.UserProfile
import com.example.changli_planet_app.Utils.RetrofitUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okio.EOFException
import okio.IOException

class UserProfileRepository private constructor() {
    companion object {
        val instance by lazy { UserProfileRepository() }
        val cache by lazy { UserDataBase.getInstance(PlanetApplication.appContext).itemDao() }
        // 缓存时间暂时为3小时
        private const val CACHE_DURATION = 3 * 60 * 60 * 1000L
    }

    private val service by lazy { RetrofitUtils.instanceUser.create(UserProfileApi::class.java) }

    fun getUserInformationById(userId: Int) = flow {
        val cacheUser = withContext(Dispatchers.IO) {
            cache.getUserById(userId)
        }

        if (cacheUser != null && System.currentTimeMillis() - cacheUser.cacheTime <= CACHE_DURATION) {
            emit(Resource.Success(cacheUser.toProfile()))
            return@flow
        }

        try {
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
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "网络错误"))

            if (cacheUser != null) {
                emit(Resource.Success(cacheUser.toProfile()))
            }
        }
    }

    fun getUserInFormationNoCache(userId: Int) = flow {
        try {
            emit(Resource.Loading())
            val result = service.getUserInformationById(userId)
            if (result.code == "200" && result.data != null) {
                val userEntity = result.data.toEntity()
                cache.insertUser(userEntity)
                emit(Resource.Success(result.data))
            } else {
                emit(Resource.Error(result.msg))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "网络错误"))
        }
    }

    fun UserProfile.toEntity(cacheTime: Long = System.currentTimeMillis()): UserEntity {
        return UserEntity(
            userId = this.userId,
            username = this.username,
            account = this.account,
            avatarUrl = this.avatarUrl,
            bio = this.bio,
            description = this.description,
            userLevel = this.userLevel,
            gender = this.gender,
            grade = this.grade,
            birthDate = this.birthDate,
            location = this.location,
            website = this.website,
            createTime = this.createTime,
            updateTime = this.updateTime,
            deleted = this.isDeleted,
            cacheTime = cacheTime
        )
    }

    fun UserEntity.toProfile(): UserProfile {
        return UserProfile(
            userId = this.userId,
            username = this.username,
            account = this.account,
            avatarUrl = this.avatarUrl,
            bio = this.bio,
            description = this.description,
            userLevel = this.userLevel,
            gender = this.gender,
            grade = this.grade,
            birthDate = this.birthDate,
            location = this.location,
            website = this.website,
            createTime = this.createTime,
            updateTime = this.updateTime,
            isDeleted = this.deleted
        )
    }
}