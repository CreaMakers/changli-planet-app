package com.zhelearn.CSUSTPlanet.common.redux.store

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.zhelearn.CSUSTPlanet.analytics.MatomoTrackerManager
import com.zhelearn.CSUSTPlanet.common.data.local.mmkv.StudentInfoManager
import com.zhelearn.CSUSTPlanet.common.data.local.mmkv.UserInfoManager
import com.zhelearn.CSUSTPlanet.common.data.remote.api.AppVersionApi
import com.zhelearn.CSUSTPlanet.common.data.remote.api.LegacyApkApi
import com.zhelearn.CSUSTPlanet.common.redux.action.UserAction
import com.zhelearn.CSUSTPlanet.common.redux.state.UserState
import com.zhelearn.CSUSTPlanet.core.PlanetApplication
import com.zhelearn.CSUSTPlanet.core.Store
import com.zhelearn.CSUSTPlanet.utils.RetrofitUtils
import com.zhelearn.CSUSTPlanet.utils.event.AppEventBus
import com.zhelearn.CSUSTPlanet.utils.event.FinishEvent
import com.zhelearn.CSUSTPlanet.widget.dialog.BindingFromWebDialog
import com.zhelearn.CSUSTPlanet.widget.dialog.NormalResponseDialog
import com.zhelearn.CSUSTPlanet.widget.dialog.UpdateDialog
import com.dcelysia.csust_spider.core.RetrofitUtils as CsustRetrofitUtils
import com.dcelysia.csust_spider.education.data.remote.EducationData
import com.dcelysia.csust_spider.core.AESUtils
import com.dcelysia.csust_spider.core.Resource
import com.dcelysia.csust_spider.education.data.remote.api.EduLoginApi
import com.dcelysia.csust_spider.mooc.data.remote.api.SSOAuthApi
import org.jsoup.Jsoup
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
                    try {
                        // 同时清除共享 SSO Cookie，必须验证本次输入，不能借用旧账号会话。
                        CsustRetrofitUtils.ClearClient("EducationClient")
                        val educationLoginResult = authenticateEducationAccount(
                            action.studentNumber,
                            action.studentPassword
                        )
                        if (educationLoginResult is Resource.Success) {
                            currentState.uiForLoading = false
                            EducationData.studentId = action.studentNumber
                            EducationData.studentPassword = action.studentPassword
                            Log.d(TAG, "教务登录成功")
                            StudentInfoManager.studentId = action.studentNumber
                            StudentInfoManager.studentPassword = action.studentPassword
                            MatomoTrackerManager.updateUserId(StudentInfoManager.studentId)
                            handler.post { AppEventBus.finishEvent.tryEmit(FinishEvent("bindingUser")) }
                            PlanetApplication.clearSchoolDataCacheAll()
                            _state.onNext(currentState)

                            // 网络教学平台只用于作业、测试和用户资料，失败不影响教务绑定。
                            runCatching {
                                val moocLoginResult = MoocRepository.instance
                                    .login(action.studentNumber, action.studentPassword)
                                    .filter { it !is com.dcelysia.csust_spider.core.Resource.Loading }
                                    .first()
                                if (moocLoginResult !is com.dcelysia.csust_spider.core.Resource.Success) {
                                    Log.w(TAG, "网络教学平台登录失败，不影响教务绑定: $moocLoginResult")
                                    return@runCatching
                                }

                                val userResource = MoocRepository.instance
                                    .getLoginUser()
                                    .filter { it !is com.dcelysia.csust_spider.core.Resource.Loading }
                                    .first()
                                // 请求期间可能切换学号，旧请求不能覆盖当前账号资料。
                                if (StudentInfoManager.studentId != action.studentNumber) {
                                    return@runCatching
                                }
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
                                    _state.onNext(currentState)
                                }
                            }.onFailure {
                                Log.w(TAG, "网络教学平台用户资料同步失败，不影响教务绑定", it)
                            }
                            return@launch
                        }

                        currentState.userStats.studentNumber = action.studentNumber
                        currentState.uiForLoading = false
                        when (educationLoginResult) {
                            is Resource.Error -> {
                                Log.d(TAG, "教务认证失败: ${educationLoginResult.msg}")
                                if (!educationLoginResult.msg.contains("请在手机网页登录一次")) {
                                    handler.post {
                                        NormalResponseDialog(
                                            action.context,
                                            educationLoginResult.msg,
                                            "绑定失败"
                                        ).show()
                                    }
                                } else {
                                    handler.post {
                                        BindingFromWebDialog(
                                            action.context,
                                            educationLoginResult.msg,
                                            "绑定失败",
                                            action.webLogin
                                        ).show()
                                    }
                                }
                            }

                            else -> {
                                handler.post {
                                    NormalResponseDialog(
                                        action.context,
                                        "学号或密码错误，请重试",
                                        "绑定失败"
                                    ).show()
                                }
                            }
                        }
                        _state.onNext(currentState)
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

    /** 绑定时使用教务的 SSO service 提交本次凭据，不经过网络教学平台。 */
    private suspend fun authenticateEducationAccount(
        studentNumber: String,
        studentPassword: String
    ): Resource<Boolean> {
        val educationService = "http://xk.csust.edu.cn/sso.jsp"
        val educationApi = CsustRetrofitUtils.instanceEduLogin.create(EduLoginApi::class.java)
        // 教务必须先建立本地会话，再接收 SSO 票据；直接提交认证会落到“用户没有登录”。
        val entryResponse = educationApi.login()
        if (!entryResponse.isSuccessful) {
            return Resource.Error("教务入口暂时不可用，请稍后重试")
        }
        val authApi = CsustRetrofitUtils.instanceSSOAuth.create(SSOAuthApi::class.java)
        val formResponse = authApi.getLoginForm(service = educationService)
        val formHtml = formResponse.body()
        if (!formResponse.isSuccessful || formHtml.isNullOrBlank()) {
            return Resource.Error("教务认证页面加载失败，请稍后重试")
        }
        val form = Jsoup.parse(formHtml)
        val passwordSalt = form.selectFirst("input#pwdEncryptSalt")?.attr("value")
        val execution = form.selectFirst("input[name=execution]")?.attr("value")
        // 没有登录表单不能视为本次凭据已验证，避免沿用旧会话误绑。
        if (passwordSalt.isNullOrBlank() || execution.isNullOrBlank()) {
            return Resource.Error("教务认证页面异常，请稍后重试")
        }
        val captchaResponse = authApi.checkNeedCaptcha(studentNumber, System.currentTimeMillis())
        val captchaStatus = captchaResponse.body()
        if (!captchaResponse.isSuccessful || captchaStatus == null) {
            return Resource.Error("教务认证状态检查失败，请稍后重试")
        }
        if (captchaStatus.isNeed) {
            return Resource.Error("账号需要验证码，请在手机网页登录一次")
        }
        val loginResponse = authApi.login(
            service = educationService,
            username = studentNumber,
            password = AESUtils.encryptPassword(studentPassword, passwordSalt),
            execution = execution
        )
        // 统一认证会用 HTTP 401 返回错误凭据，不能归为网络请求失败。
        if (loginResponse.code() == 401) {
            return Resource.Error("学号或密码错误，请重试")
        }
        val loginHtml = loginResponse.body()
        if (!loginResponse.isSuccessful || loginHtml.isNullOrBlank()) {
            return Resource.Error("教务登录请求失败，请稍后重试")
        }
        val loginUrl = loginResponse.raw().request.url
        if (loginUrl.host == "authserver.csust.edu.cn" &&
            loginUrl.encodedPath == "/authserver/login"
        ) {
            val loginDocument = Jsoup.parse(loginHtml)
            if (loginDocument.selectFirst("input[name=username]") != null) {
                return Resource.Error("学号或密码错误，请重试")
            }
            return Resource.Error("教务认证未完成，请在手机网页登录一次")
        }
        if (loginUrl.host != "xk.csust.edu.cn") {
            return Resource.Error("教务登录跳转异常，请稍后重试")
        }
        // 再访问受保护的学生主页，不能仅凭页面缺少某段提示文字判断成功。
        val homeResponse = educationApi.checkLoginStates()
        val homeHtml = homeResponse.body()
        val homeUrl = homeResponse.raw().request.url
        if (!homeResponse.isSuccessful || homeHtml.isNullOrBlank() ||
            homeUrl.host != "xk.csust.edu.cn" ||
            homeUrl.encodedPath != "/jsxsd/framework/xsMain.jsp" ||
            Jsoup.parse(homeHtml).selectFirst("input[name=username], input[type=password]") != null
        ) {
            Log.w(TAG, "教务会话建立失败: HTTP ${homeResponse.code()}, " +
                "path=${homeUrl.encodedPath.substringBefore(';')}")
            return Resource.Error("统一认证已通过，但教务会话建立失败，请重新登录")
        }
        return Resource.Success(true)
    }

    fun getUserState(): UserState = currentState
}
