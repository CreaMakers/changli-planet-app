package com.example.changli_planet_app.Activity.Store

import android.os.Handler
import android.os.Looper
import com.example.changli_planet_app.Activity.Action.UserAction
import com.example.changli_planet_app.Activity.State.UserState
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Store
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.ApkResponse
import com.example.changli_planet_app.Network.Response.UploadAvatarResponse
import com.example.changli_planet_app.Network.Response.UserProfileResponse
import com.example.changli_planet_app.Network.Response.UserStatsResponse
import com.example.changli_planet_app.Util.Event.FinishEvent
import com.example.changli_planet_app.Util.EventBusHelper
import com.example.changli_planet_app.Widget.Dialog.UpdateDialog
import com.example.changli_planet_app.Widget.View.CustomToast
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response

class UserStore : Store<UserState, UserAction>() {

    private val TAG = "UserStore"

    companion object {
        private var currentState = UserState()
    }

    private val handler = Handler(Looper.getMainLooper())
    override fun handleEvent(action: UserAction) {
        currentState = when (action) {
            is UserAction.GetCurrentUserProfile -> {
                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .get(PlanetApplication.UserIp + "/me/profile")
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
                                    currentState.userProfile = it
                                    currentState.avatarUri = it.avatarUrl
                                }
                            }

                            else -> {
                                handler.post {
                                    CustomToast.showMessage(
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
                            CustomToast.showMessage(action.context, "获取用户信息失败")
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
                    .get(PlanetApplication.UserIp + "/me/stats")
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
                                    CustomToast.showMessage(
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
                            CustomToast.showMessage(action.context, "获取用户动态信息失败")
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
                    .post(PlanetApplication.UserIp + "/me/avatar")
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
                                currentState.avatarUri = fromJson.data.toString()
                                currentState.userProfile.avatarUrl = fromJson.data.toString()
                            }

                            else -> {
                                CustomToast.showMessage(
                                    PlanetApplication.appContext,
                                    "请求失败, ${fromJson.msg}"
                                )
                            }
                        }
                    }

                    override fun onFailure(error: String) {
                    }

                })
                _state.onNext(currentState)
                currentState
            }

            is UserAction.UpdateUserProfile -> {
                action.userProfileRequest.avatarUrl = currentState.avatarUri
                action.userProfileRequest.userLevel = currentState.userProfile.userLevel
                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .put(PlanetApplication.UserIp + "/me/profile")
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
                                handler.post {
                                    CustomToast.showMessage(action.context, "更改成功(ฅ′ω`ฅ)")
                                    EventBusHelper.post(FinishEvent("updateUser"))
                                }
                            }

                            else -> {
                                handler.post {
                                    CustomToast.showMessage(
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
                            CustomToast.showMessage(action.context, "获取用户动态信息失败")
                        }
                        _state.onNext(currentState)
                    }
                })
                currentState
            }

            is UserAction.QueryIsLastedApk -> {
                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .get(PlanetApplication.UserIp + "/apk")
                    .addQueryParam("versionCode", action.versionCode.toString())
                    .addQueryParam("versionName", action.versionName)
                    .build()
                OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                    override fun onSuccess(response: Response) {
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
                                    CustomToast.showMessage(
                                        action.context,
                                        "获取最新apk失败, ${fromJson.msg}"
                                    )
                                }
                            }
                        }
                    }

                    override fun onFailure(error: String) {
                        handler.post {
                            CustomToast.showMessage(action.context, "获取用户动态信息失败")
                        }
                    }
                })
                currentState
            }
        }
    }

    fun getUserState(): UserState {
        return currentState
    }
}