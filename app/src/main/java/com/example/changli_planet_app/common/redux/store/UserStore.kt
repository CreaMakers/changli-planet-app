package com.example.changli_planet_app.common.redux.store

import android.os.Handler
import android.os.Looper
import com.example.changli_planet_app.common.data.local.mmkv.StudentInfoManager
import com.example.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.example.changli_planet_app.common.data.local.room.database.UserDataBase
import com.example.changli_planet_app.common.data.remote.dto.ApkResponse
import com.example.changli_planet_app.common.data.remote.dto.UploadAvatarResponse
import com.example.changli_planet_app.common.data.remote.dto.UserProfileResponse
import com.example.changli_planet_app.common.data.remote.dto.UserStatsResponse
import com.example.changli_planet_app.common.redux.action.UserAction
import com.example.changli_planet_app.common.redux.state.UserState
import com.example.changli_planet_app.core.PlanetApplication
import com.example.changli_planet_app.core.Store
import com.example.changli_planet_app.core.network.HttpUrlHelper
import com.example.changli_planet_app.core.network.MyResponse
import com.example.changli_planet_app.core.network.OkHttpHelper
import com.example.changli_planet_app.core.network.listener.RequestCallback
import com.example.changli_planet_app.settings.redux.store.BindingUserStore
import com.example.changli_planet_app.utils.Event.FinishEvent
import com.example.changli_planet_app.utils.EventBusHelper
import com.example.changli_planet_app.utils.toEntity
import com.example.changli_planet_app.widget.Dialog.NormalResponseDialog
import com.example.changli_planet_app.widget.Dialog.UpdateDialog
import com.example.changli_planet_app.widget.View.CustomToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response

class UserStore : Store<UserState, UserAction>() {

    private val TAG = "UserStore"

    companion object {
        private var currentState = UserState()
    }

    val cache by lazy { UserDataBase.Companion.getInstance(PlanetApplication.Companion.appContext).itemDao() }

    private val handler = Handler(Looper.getMainLooper())
    override fun handleEvent(action: UserAction) {
        currentState = when (action) {
            is UserAction.GetCurrentUserProfile -> {
                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .get(PlanetApplication.Companion.UserIp + "/me/profile")
                    .build()
                OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        val fromJson = OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            UserProfileResponse::class.java
                        )
                        when (fromJson.code) {
                            "200" -> {
                                fromJson.data?.let {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        cache.insertUser(it.toEntity())
                                    }
                                    UserInfoManager.account = it.account
                                    UserInfoManager.userAvatar = it.avatarUrl
                                    UserInfoManager.userId = it.userId
                                    UserInfoManager.userEmail = it.emailbox ?: "待绑定"
                                    currentState.avatarUri = it.avatarUrl
                                    it.location = if (currentState.locationChangedManually) //防止数据刷新覆盖选择结果
                                        currentState.userProfile.location
                                    else
                                        it.location
                                    if (currentState.locationChangedManually) currentState.locationChangedManually = false
                                    currentState.userProfile = it

                                }
                            }

                            else -> {
                                handler.post {
                                    CustomToast.Companion.showMessage(
                                        action.context,
                                        "请求失败, ${fromJson.msg}"
                                    )
                                }
                            }
                        }
                        _state.onNext(currentState)
                    }

                    override fun onFailure(error: String) {
                        handler.post {
                            CustomToast.Companion.showMessage(action.context, "获取用户信息失败")
                        }
                        _state.onNext(currentState)
                    }
                })
                currentState
            }

            is UserAction.initilaize -> {
                _state.onNext(currentState)
                currentState
            }

            is UserAction.GetCurrentUserStats -> {
                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .get(PlanetApplication.Companion.UserIp + "/me/stats")
                    .build()
                OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        val fromJson = OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            UserStatsResponse::class.java
                        )
                        when (fromJson.code) {
                            "200" -> {
                                fromJson.data?.let {
                                    currentState.userStats = it
                                }
                            }

                            else -> {
                                handler.post {
                                    CustomToast.Companion.showMessage(
                                        action.context,
                                        "请求失败, ${fromJson.msg}"
                                    )
                                }
                            }
                        }

                        _state.onNext(currentState)
                    }

                    override fun onFailure(error: String) {
                        handler.post {
                            CustomToast.Companion.showMessage(action.context, "获取用户动态信息失败")
                        }
                        _state.onNext(currentState)
                    }
                })
                currentState
            }

            is UserAction.UpdateAvatar -> {
                currentState.avatarUri = action.uri
                _state.onNext(currentState)
                currentState
            }

            is UserAction.UploadAvatar -> {
                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .post(PlanetApplication.Companion.UserIp + "/me/avatar")
                    .addFieldPart(
                        "avatar",
                        action.file,
                        "image/*".toMediaTypeOrNull()
                    )
                    .build()
                OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        val fromJson = OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            UploadAvatarResponse::class.java
                        )
                        when (fromJson.code) {
                            "200" -> {
                                UserInfoManager.userAvatar = fromJson.data.toString()
                                currentState.avatarUri = fromJson.data.toString()
                                currentState.userProfile.avatarUrl = fromJson.data.toString()
                                _state.onNext(currentState)
                            }

                            else -> {
                                CustomToast.Companion.showMessage(
                                    PlanetApplication.Companion.appContext,
                                    "请求失败, ${fromJson.msg}"
                                )
                                _state.onNext(currentState)
                            }
                        }
                    }

                    override fun onFailure(error: String) {
                    }

                })

                currentState
            }

            is UserAction.UpdateUserProfile -> {
                action.userProfileRequest.avatarUrl = currentState.userProfile.avatarUrl
                action.userProfileRequest.userLevel = currentState.userProfile.userLevel
                action.userProfileRequest.location = currentState.userProfile.location

                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .put(PlanetApplication.Companion.UserIp + "/me/profile")
                    .body(OkHttpHelper.gson.toJson(action.userProfileRequest))
                    .build()
                OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        val fromJson = OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            UserProfileResponse::class.java
                        )
                        when (fromJson.code) {
                            "200" -> {
                                currentState.userProfile = fromJson.data!!
                                UserInfoManager.account = fromJson.data.account
                                handler.post {
                                    EventBusHelper.post(FinishEvent("updateUser"))
                                }
                            }

                            else -> {
                                handler.post {
                                    CustomToast.Companion.showMessage(
                                        action.context,
                                        "提交失败, ${fromJson.msg}"
                                    )
                                }
                            }
                        }

                        _state.onNext(currentState)
                    }

                    override fun onFailure(error: String) {
                        handler.post {
                            CustomToast.Companion.showMessage(action.context, "获取用户动态信息失败")
                        }
                        _state.onNext(currentState)
                    }
                })
                currentState
            }

            is UserAction.QueryIsLastedApk -> {
                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .get(PlanetApplication.Companion.UserIp + "/apk")
                    .addQueryParam("versionCode", action.versionCode.toString())
                    .addQueryParam("versionName", action.versionName)
                    .build()
                OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        try {
                            val fromJson = OkHttpHelper.gson.fromJson(
                                response.body?.string(),
                                ApkResponse::class.java
                            )

                            when (fromJson.code) {
                                "200" -> {
                                    if (fromJson.msg == "获取最新apk版本成功") {
                                        val data = fromJson.data!!
                                        handler.post {
                                            UpdateDialog(
                                                action.context,
                                                data.updateMessage,
                                                data.downloadUrl
                                            ).show()
                                        }
                                    }
                                }

                                else -> {
                                    handler.post {
                                        CustomToast.Companion.showMessage(
                                            action.context,
                                            "获取最新apk失败, ${fromJson.msg}"
                                        )
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            handler.post {
                                CustomToast.Companion.showMessage(action.context, "获取新版本失败")
                            }
                        }

                    }

                    override fun onFailure(error: String) {
                        handler.post {
                            CustomToast.Companion.showMessage(action.context, "获取用户动态信息失败")
                        }
                    }
                })
                currentState
            }

            is UserAction.BindingStudentNumber -> {
                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .post(PlanetApplication.Companion.UserIp + "/me/student-number")
                    .body(OkHttpHelper.gson.toJson(BindingUserStore.StudentNumberRequest(action.student_number)))
                    .build()

                OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        try {
                            val fromJson = OkHttpHelper.gson.fromJson(
                                response.body?.string(),
                                MyResponse::class.java
                            )
                            when (fromJson.code) {
                                "200" -> {
                                    currentState.userStats.studentNumber = action.student_number
                                    StudentInfoManager.studentId = action.student_number
                                    handler.post {
                                        EventBusHelper.post(FinishEvent("bindingUser"))


                                    }
                                    PlanetApplication.clearCacheAll()
                                }

                                else -> {
                                    handler.post {
                                        NormalResponseDialog(
                                            action.context,
                                            fromJson.msg,
                                            "绑定失败"
                                        ).show()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            handler.post {
                                NormalResponseDialog(
                                    action.context,
                                    "数据解析错误",
                                    "绑定失败"
                                ).show()
                            }
                        }
                    }

                    override fun onFailure(error: String) {
                        handler.post {
                            NormalResponseDialog(
                                action.context,
                                "网络请求失败",
                                "绑定失败"
                            ).show()
                        }
                    }
                })
                _state.onNext(currentState)
                currentState
            }
            is UserAction.UpdateLocation->{
                currentState.userProfile.location = action.location
                currentState.locationChangedManually = true //表示所在地已在本地更新
                _state.onNext(currentState)
                currentState
            }
            else -> {
                _state.onNext(currentState)
                currentState
            }
        }
    }

    fun getUserState(): UserState {
        return currentState
    }
}