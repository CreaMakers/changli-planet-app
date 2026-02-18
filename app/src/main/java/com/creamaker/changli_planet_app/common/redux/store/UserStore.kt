package com.creamaker.changli_planet_app.common.redux.store

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.creamaker.changli_planet_app.common.data.local.mmkv.StudentInfoManager
import com.creamaker.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.creamaker.changli_planet_app.common.data.local.room.database.UserDataBase
import com.creamaker.changli_planet_app.common.data.remote.dto.ApkResponse
import com.creamaker.changli_planet_app.common.data.remote.dto.UploadAvatarResponse
import com.creamaker.changli_planet_app.common.data.remote.dto.UserProfileResponse
import com.creamaker.changli_planet_app.common.data.remote.dto.UserStatsResponse
import com.creamaker.changli_planet_app.common.redux.action.UserAction
import com.creamaker.changli_planet_app.common.redux.state.UserState
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.Store
import com.creamaker.changli_planet_app.core.network.HttpUrlHelper
import com.creamaker.changli_planet_app.core.network.OkHttpHelper
import com.creamaker.changli_planet_app.core.network.listener.RequestCallback
import com.creamaker.changli_planet_app.utils.EventBusHelper
import com.creamaker.changli_planet_app.utils.event.FinishEvent
import com.creamaker.changli_planet_app.utils.toEntity
import com.creamaker.changli_planet_app.widget.dialog.BindingFromWebDialog
import com.creamaker.changli_planet_app.widget.dialog.NormalResponseDialog
import com.creamaker.changli_planet_app.widget.dialog.UpdateDialog
import com.creamaker.changli_planet_app.widget.view.CustomToast
import com.dcelysia.csust_spider.core.RetrofitUtils
import com.dcelysia.csust_spider.education.data.remote.EducationData
import com.dcelysia.csust_spider.education.data.remote.services.AuthService
import com.dcelysia.csust_spider.mooc.data.remote.repository.MoocRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
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

                            //修改
//                            else -> {
//                                Log.d("Trainer", "OK!200")
//                                handler.post {
//                                    CustomToast.Companion.showMessage(
//                                        action.context,
//                                        "请求失败, ${fromJson.msg}"
//                                    )
//                                }
//                            }
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

                            //修改
//                            else -> {
//                                handler.post {
//                                    Log.d("Trainer", "!200")
//                                    CustomToast.Companion.showMessage(
//                                        action.context,
//                                        "请求失败, ${fromJson.msg}"
//                                    )
//                                }
//                            }
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
                                Log.d(TAG,"返回"+fromJson.data.toString())
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
                                response.body.string(),
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
                currentState.uiForLoading = true

                CoroutineScope(Dispatchers.IO).launch {
                    RetrofitUtils.ClearClient("moocClient")
                    RetrofitUtils.ClearClient("EducationClient")
                    try {
                        // 先做 SSO 登录（过滤掉 Loading）
                        val ssoResult = MoocRepository.instance
                            .login(action.student_number, StudentInfoManager.studentPassword)
                            .filter { it !is com.dcelysia.csust_spider.core.Resource.Loading }
                            .first()
                        when (ssoResult) {
                            is com.dcelysia.csust_spider.core.Resource.Success -> {
                                Log.d(TAG, "sso登陆成功")
                                // SSO 成功后再做教务登录（顺序执行）
                                val eduSuccess = AuthService.Login(
                                    action.student_number,
                                    StudentInfoManager.studentPassword
                                )
                                if (eduSuccess) {
                                    currentState.uiForLoading = false

                                    EducationData.studentId = action.student_number
                                    EducationData.studentPassword = StudentInfoManager.studentPassword
                                    Log.d(TAG, "教务登录成功")
                                    StudentInfoManager.studentId = action.student_number
                                    handler.post { EventBusHelper.post(FinishEvent("bindingUser")) }
                                    PlanetApplication.clearSchoolDataCacheAll()
                                    _state.onNext(currentState)
                                } else {
                                    currentState.userStats.studentNumber = action.student_number
                                    currentState.uiForLoading = false
                                    handler.post {
                                        NormalResponseDialog(
                                            action.context,
                                            "学号或密码错误，请重试",
                                            "绑定失败"
                                        ).show()
                                    }
                                }
                            }

                            is com.dcelysia.csust_spider.core.Resource.Error -> {
                                currentState.userStats.studentNumber = action.student_number
                                currentState.uiForLoading = false
                                Log.d(TAG,"ssoResult:${ssoResult}")
                                //如果不用重新在网页登录就不用显示出网页登录选项
                                if (!(ssoResult.msg.contains("请在手机网页登录一次"))){
                                    handler.post {
                                        NormalResponseDialog(
                                            action.context,
                                            "学号或密码错误，请重试",
                                            "绑定失败"
                                        ).show()
                                    }
                                }
                                else{
                                    handler.post {
                                        BindingFromWebDialog(
                                            action.context,
                                            ssoResult.msg ?: "SSO 登录失败",
                                            "绑定失败",
                                            action.webLogin
                                        ).show()
                                    }
                                }

                                _state.onNext(currentState)
                            }

                            else -> {
                                currentState.userStats.studentNumber = action.student_number
                                currentState.uiForLoading = false
                                // 兜底（理论上 filter 已去掉 Loading）
                                handler.post {
                                    NormalResponseDialog(
                                        action.context,
                                        "网络或未知错误，请重试",
                                        "绑定失败"
                                    ).show()
                                }
                                _state.onNext(currentState)
                            }
                        }
                    } catch (e: Exception) {
                        currentState.userStats.studentNumber = action.student_number
                        currentState.uiForLoading = false
                        e.printStackTrace()
                        handler.post {
                            NormalResponseDialog(
                                action.context,
                                "网络错误: ${e.message ?: "未知"}",
                                "绑定失败"
                            ).show()
                        }
                        _state.onNext(currentState)
                    }
                }
                _state.onNext(currentState)
                currentState
            }
            is UserAction.WebLoginSuccess ->{
                // 1. 更新 StudentInfoManager
                StudentInfoManager.studentId = action.account
                StudentInfoManager.studentPassword = action.password
                // 2. 更新 State，这将通过 observeState 回调来更新UI
                currentState.userStats = currentState.userStats.copy(studentNumber = action.account)
                _state.onNext(currentState)
                // 3. 调用现有的学号绑定逻辑，触发网络请求
                handleEvent(UserAction.BindingStudentNumber(action.context, action.account) {})

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