package com.creamaker.changli_planet_app.common.redux.store

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.creamaker.changli_planet_app.common.data.local.mmkv.StudentInfoManager
import com.creamaker.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.creamaker.changli_planet_app.common.data.remote.api.AppVersionApi
import com.creamaker.changli_planet_app.common.data.remote.api.LegacyApkApi
import com.creamaker.changli_planet_app.common.redux.action.UserAction
import com.creamaker.changli_planet_app.common.redux.state.UserState
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.Store
import com.creamaker.changli_planet_app.utils.EventBusHelper
import com.creamaker.changli_planet_app.utils.RetrofitUtils
import com.creamaker.changli_planet_app.utils.event.FinishEvent
import com.creamaker.changli_planet_app.widget.dialog.BindingFromWebDialog
import com.creamaker.changli_planet_app.widget.dialog.NormalResponseDialog
import com.creamaker.changli_planet_app.widget.dialog.UpdateDialog
import com.dcelysia.csust_spider.core.RetrofitUtils as CsustRetrofitUtils
import com.dcelysia.csust_spider.education.data.remote.EducationData
import com.dcelysia.csust_spider.education.data.remote.services.AuthService
import com.dcelysia.csust_spider.mooc.data.remote.repository.MoocRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UserStore : Store<UserState, UserAction>() {
    companion object {
        private const val TAG = "UserStore"
        private var currentState = UserState()
    }

    private val handler = Handler(Looper.getMainLooper())

    override fun handleEvent(action: UserAction) {
        currentState = when (action) {
            is UserAction.initilaize -> {
                _state.onNext(currentState)
                currentState
            }

            is UserAction.QueryIsLastedApk -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val newApi = RetrofitUtils.instancePlanet.create(AppVersionApi::class.java)
                    val newResult = runCatching {
                        newApi.checkUpdate(
                            platform = "android",
                            currentVersionCode = action.versionCode
                        )
                    }

                    val reachedNewApi = newResult.isSuccess
                    newResult.getOrNull()?.let { resp ->
                        val latest = resp.latestVersion
                        if (resp.hasUpdate && latest != null && latest.downloadUrl.isNotBlank()) {
                            handler.post {
                                UpdateDialog(
                                    action.context,
                                    latest.releaseNotes,
                                    latest.downloadUrl
                                ).show()
                            }
                        }
                    }

                    if (reachedNewApi) return@launch

                    val err = newResult.exceptionOrNull()
                    Log.w(TAG, "新版应用更新接口失败，降级到旧接口兜底", err)

                    runCatching {
                        val legacyApi = RetrofitUtils.instanceUser.create(LegacyApkApi::class.java)
                        legacyApi.queryLatestApk(
                            versionCode = action.versionCode.toString(),
                            versionName = action.versionName
                        )
                    }.onSuccess { legacyResp ->
                        if (legacyResp.code == "200" && legacyResp.msg == "获取最新apk版本成功") {
                            legacyResp.data?.let { data ->
                                if (data.downloadUrl.isNotBlank()) {
                                    handler.post {
                                        UpdateDialog(
                                            action.context,
                                            data.updateMessage,
                                            data.downloadUrl
                                        ).show()
                                    }
                                }
                            }
                        } else {
                            Log.w(
                                TAG,
                                "旧接口返回非成功：code=${legacyResp.code}, msg=${legacyResp.msg}"
                            )
                        }
                    }.onFailure { e ->
                        Log.w(TAG, "旧版应用更新接口也失败，放弃本次检查", e)
                        // 两个接口都失败：静默，不打扰用户。
                    }
                }
                currentState
            }

            is UserAction.BindingStudentNumber -> {
                currentState.uiForLoading = true
                CoroutineScope(Dispatchers.IO).launch {
                    CsustRetrofitUtils.ClearClient("moocClient")
                    CsustRetrofitUtils.ClearClient("EducationClient")
                    try {
                        val ssoResult = MoocRepository.instance
                            .login(action.studentNumber, action.studentPassword)
                            .filter { it !is com.dcelysia.csust_spider.core.Resource.Loading }
                            .first()
                        when (ssoResult) {
                            is com.dcelysia.csust_spider.core.Resource.Success -> {
                                Log.d(TAG, "sso登陆成功")
                                // SSO 登录成功后抓取 MOOC 用户信息并更新头像 / 用户名，失败不影响主流程
                                runCatching {
                                    val userResource = MoocRepository.instance
                                        .getLoginUser()
                                        .filter { it !is com.dcelysia.csust_spider.core.Resource.Loading }
                                        .first()
                                    if (userResource is com.dcelysia.csust_spider.core.Resource.Success) {
                                        userResource.data?.let { sso ->
                                            val avatarUrl = sso.avatar
                                            if (avatarUrl.isNotBlank()) {
                                                UserInfoManager.userAvatar = avatarUrl
                                                currentState.avatarUri = avatarUrl
                                                currentState.userProfile.avatarUrl = avatarUrl
                                            }
                                            if (sso.userName.isNotBlank()) {
                                                UserInfoManager.account = sso.userName
                                            }
                                        }
                                    }
                                }.onFailure { Log.w(TAG, "获取 SSO 用户信息失败", it) }

                                val eduSuccess = AuthService.login(
                                    action.studentNumber,
                                    action.studentPassword
                                )
                                if (eduSuccess) {
                                    currentState.uiForLoading = false
                                    EducationData.studentId = action.studentNumber
                                    EducationData.studentPassword = action.studentPassword
                                    Log.d(TAG, "教务登录成功")
                                    StudentInfoManager.studentId = action.studentNumber
                                    StudentInfoManager.studentPassword = action.studentPassword
                                    handler.post { EventBusHelper.post(FinishEvent("bindingUser")) }
                                    PlanetApplication.clearSchoolDataCacheAll()
                                    _state.onNext(currentState)
                                } else {
                                    currentState.userStats.studentNumber = action.studentNumber
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
                                currentState.userStats.studentNumber = action.studentNumber
                                currentState.uiForLoading = false
                                Log.d(TAG, "ssoResult:${ssoResult}")
                                if (!(ssoResult.msg.contains("请在手机网页登录一次"))) {
                                    handler.post {
                                        NormalResponseDialog(
                                            action.context,
                                            ssoResult.msg,
                                            "绑定失败"
                                        ).show()
                                    }
                                } else {
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
                                currentState.userStats.studentNumber = action.studentNumber
                                currentState.uiForLoading = false
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
                        currentState.userStats.studentNumber = action.studentNumber
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

            is UserAction.WebLoginSuccess -> {
                StudentInfoManager.studentId = action.account
                StudentInfoManager.studentPassword = action.password
                currentState.userStats =
                    currentState.userStats.copy(studentNumber = action.account)
                _state.onNext(currentState)
                handleEvent(
                    UserAction.BindingStudentNumber(
                        action.context,
                        action.account,
                        action.password
                    ) {}
                )
                currentState
            }

            is UserAction.RefreshMoocProfileSilently -> {
                val studentId = StudentInfoManager.studentId
                val studentPassword = StudentInfoManager.studentPassword
                val alreadyHasProfile = UserInfoManager.account.isNotBlank()
                val canRefresh = studentId.isNotBlank() &&
                    studentPassword.isNotBlank() &&
                    !alreadyHasProfile
                if (canRefresh) {
                    CoroutineScope(Dispatchers.IO).launch {
                        runCatching {
                            val ssoResult = MoocRepository.instance
                                .login(studentId, studentPassword)
                                .filter { it !is com.dcelysia.csust_spider.core.Resource.Loading }
                                .first()
                            if (ssoResult !is com.dcelysia.csust_spider.core.Resource.Success) {
                                return@runCatching
                            }

                            val userResource = MoocRepository.instance
                                .getLoginUser()
                                .filter { it !is com.dcelysia.csust_spider.core.Resource.Loading }
                                .first()
                            if (userResource !is com.dcelysia.csust_spider.core.Resource.Success) {
                                return@runCatching
                            }

                            userResource.data?.let { sso ->
                                if (sso.avatar.isNotBlank()) {
                                    UserInfoManager.userAvatar = sso.avatar
                                }
                                if (sso.userName.isNotBlank()) {
                                    UserInfoManager.account = sso.userName
                                }
                            }
                        }.onFailure { Log.w(TAG, "静默刷新 MOOC 用户资料失败", it) }
                    }
                }
                currentState
            }
        }
    }

    fun getUserState(): UserState = currentState
}
