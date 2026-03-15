package com.creamaker.changli_planet_app.auth.mvi

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.auth.data.remote.dto.UserEmail
import com.creamaker.changli_planet_app.base.mvi.BaseMviViewModel
import com.creamaker.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.network.HttpUrlHelper
import com.creamaker.changli_planet_app.core.network.MyResponse
import com.creamaker.changli_planet_app.core.network.OkHttpHelper
import com.creamaker.changli_planet_app.core.network.listener.RequestCallback
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.Response

sealed interface LoginByEmailIntent {
    data object Initialize : LoginByEmailIntent
    data class InputEmail(val value: TextFieldValue) : LoginByEmailIntent
    data class InputCaptcha(val value: TextFieldValue) : LoginByEmailIntent
    data class CheckAgreement(val checked: Boolean) : LoginByEmailIntent
    data object ClickGetCaptcha : LoginByEmailIntent
    data object ClickLogin : LoginByEmailIntent
    data object ClickForgetPassword : LoginByEmailIntent
    data object ClickAccountLogin : LoginByEmailIntent
    data object ClickRegister : LoginByEmailIntent
    data object ClickBack : LoginByEmailIntent
    data object DismissDialog : LoginByEmailIntent
}

sealed interface LoginByEmailEffect {
    data object LoginSuccess : LoginByEmailEffect
    data class ShowToast(val message: String) : LoginByEmailEffect
}

data class LoginByEmailUiState(
    val email: TextFieldValue = TextFieldValue(""),
    val captcha: TextFieldValue = TextFieldValue(""),
    val checked: Boolean = false,
    val canLogin: Boolean = false,
    val countDown: Int = 0,
    val isCountDown: Boolean = false,
    val dialog: AuthDialogState? = null
)

class LoginByEmailViewModel : BaseMviViewModel<LoginByEmailIntent, LoginByEmailUiState>(LoginByEmailUiState()) {
    private var countDownJob: Job? = null
    private val _effect = MutableSharedFlow<LoginByEmailEffect>(extraBufferCapacity = 1)
    val effect: SharedFlow<LoginByEmailEffect> = _effect.asSharedFlow()

    override fun process(intent: LoginByEmailIntent) {
        when (intent) {
            LoginByEmailIntent.Initialize -> setState(LoginByEmailUiState())
            is LoginByEmailIntent.InputEmail -> {
                setState(
                    currentState.copy(
                        email = intent.value,
                        canLogin = canLogin(intent.value, currentState.captcha, currentState.checked)
                    )
                )
            }
            is LoginByEmailIntent.InputCaptcha -> {
                val filtered = intent.value.text.filter { it.isDigit() }.take(4)
                val captcha = intent.value.copy(
                    text = filtered,
                    selection = TextRange(
                        intent.value.selection.start.coerceAtMost(filtered.length),
                        intent.value.selection.end.coerceAtMost(filtered.length)
                    )
                )
                setState(
                    currentState.copy(
                        captcha = captcha,
                        canLogin = canLogin(currentState.email, captcha, currentState.checked)
                    )
                )
            }
            is LoginByEmailIntent.CheckAgreement -> {
                setState(
                    currentState.copy(
                        checked = intent.checked,
                        canLogin = canLogin(currentState.email, currentState.captcha, intent.checked)
                    )
                )
            }
            LoginByEmailIntent.ClickGetCaptcha -> {
                if (currentState.email.text.isNotEmpty() && !currentState.isCountDown) {
                    getCaptchaByLogin()
                }
            }
            LoginByEmailIntent.DismissDialog -> {
                setState(currentState.copy(dialog = null))
            }
            else -> Unit
        }
    }

    fun loginByEmail() {
        viewModelScope.launch {
            val s = currentState
            val request = UserEmail(s.email.text, s.captcha.text)
            val builder = HttpUrlHelper.HttpRequest()
                .post(PlanetApplication.UserIp + "/sessions/email")
                .header("deviceId", PlanetApplication.getSystemDeviceId())
                .body(OkHttpHelper.gson.toJson(request))
                .build()
            OkHttpHelper.sendRequest(builder, object : RequestCallback {
                override fun onSuccess(response: Response) {
                    val fromJson = OkHttpHelper.gson.fromJson(response.body?.string(), MyResponse::class.java)
                    if (fromJson.msg == "用户登录成功") {
                        PlanetApplication.isExpired = false
                        UserInfoManager.userEmail = request.email
                        PlanetApplication.accessToken = response.header("Authorization", "") ?: ""
                        _effect.tryEmit(LoginByEmailEffect.LoginSuccess)
                    } else {
                        setState(
                            currentState.copy(
                                dialog = AuthDialogState(
                                    title = "登录失败",
                                    message = normalizeDialogMessage(fromJson.msg)
                                )
                            )
                        )
                    }
                }

                override fun onFailure(error: String) {
                }
            })
        }
    }

    override fun onCleared() {
        countDownJob?.cancel()
        super.onCleared()
    }

    private fun canLogin(email: TextFieldValue, captcha: TextFieldValue, checked: Boolean): Boolean {
        return email.text.isNotEmpty() && captcha.text.isNotEmpty() && checked
    }

    private fun getCaptchaByLogin() {
        val builder = HttpUrlHelper.HttpRequest()
            .post(PlanetApplication.UserIp + "/auth/verification-code/login")
            .body(OkHttpHelper.gson.toJson(com.creamaker.changli_planet_app.auth.data.remote.dto.Email(currentState.email.text)))
            .build()
        OkHttpHelper.sendRequest(builder, object : RequestCallback {
            override fun onSuccess(response: Response) {
                val fromJson = OkHttpHelper.gson.fromJson(response.body?.string(), MyResponse::class.java)
                if (fromJson.msg == "验证码已发送") {
                    startCountDown()
                } else {
                    _effect.tryEmit(LoginByEmailEffect.ShowToast("发送失败"))
                }
            }

            override fun onFailure(error: String) {
            }
        })
    }

    private fun startCountDown() {
        countDownJob?.cancel()
        countDownJob = viewModelScope.launch {
            setState(currentState.copy(isCountDown = true))
            val totalTime = 60
            repeat(totalTime) { index ->
                setState(currentState.copy(countDown = totalTime - index))
                delay(1000)
            }
            setState(currentState.copy(countDown = 0, isCountDown = false))
        }
    }

    private fun normalizeDialogMessage(message: String): String {
        return if (message == "无效的凭证") "账号或密码错误" else message
    }
}
