package com.creamaker.changli_planet_app.auth.mvi

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.auth.data.remote.dto.Email
import com.creamaker.changli_planet_app.auth.data.remote.dto.UserPasswordAndEmail
import com.creamaker.changli_planet_app.base.mvi.BaseMviViewModel
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

sealed interface BindEmailIntent {
    data class Initialize(val account: String, val password: String) : BindEmailIntent
    data class InputEmail(val value: TextFieldValue) : BindEmailIntent
    data class InputCaptcha(val value: TextFieldValue) : BindEmailIntent
    data object ClickGetCaptcha : BindEmailIntent
    data object ClickBind : BindEmailIntent
    data object DismissDialog : BindEmailIntent
}

sealed interface BindEmailEffect {
    data class BindSuccess(val username: String, val password: String) : BindEmailEffect
    data class ShowToast(val message: String) : BindEmailEffect
}

data class BindEmailUiState(
    val email: TextFieldValue = TextFieldValue(""),
    val captcha: TextFieldValue = TextFieldValue(""),
    val canBind: Boolean = false,
    val isCountDown: Boolean = false,
    val countDown: Int = 0,
    val dialog: AuthDialogState? = null
)

class BindEmailViewModel : BaseMviViewModel<BindEmailIntent, BindEmailUiState>(BindEmailUiState()) {
    private var initAccount: String = ""
    private var initPassword: String = ""
    private var countDownJob: Job? = null
    private val _effect = MutableSharedFlow<BindEmailEffect>(extraBufferCapacity = 1)
    val effect: SharedFlow<BindEmailEffect> = _effect.asSharedFlow()

    override fun process(intent: BindEmailIntent) {
        when (intent) {
            is BindEmailIntent.Initialize -> {
                initAccount = intent.account
                initPassword = intent.password
                setState(BindEmailUiState())
            }
            is BindEmailIntent.InputEmail -> {
                setState(
                    currentState.copy(
                        email = intent.value,
                        canBind = canBind(intent.value, currentState.captcha)
                    )
                )
            }
            is BindEmailIntent.InputCaptcha -> {
                val filtered = intent.value.text.filter { it.isDigit() }.take(4)
                val captcha = intent.value.copy(
                    text = filtered,
                    selection = TextRange(
                        intent.value.selection.start.coerceAtMost(filtered.length),
                        intent.value.selection.end.coerceAtMost(filtered.length)
                    )
                )
                setState(currentState.copy(captcha = captcha, canBind = canBind(currentState.email, captcha)))
            }
            BindEmailIntent.ClickGetCaptcha -> {
                if (currentState.email.text.isNotEmpty() && !currentState.isCountDown) {
                    getCaptcha()
                }
            }
            BindEmailIntent.DismissDialog -> {
                setState(currentState.copy(dialog = null))
            }
            else -> Unit
        }
    }

    fun bind() {
        viewModelScope.launch {
            val newUser = UserPasswordAndEmail(
                username = initAccount,
                password = initPassword,
                email = currentState.email.text,
                verifyCode = currentState.captcha.text
            )
            val builder = HttpUrlHelper.HttpRequest()
                .post(PlanetApplication.UserIp + "/register")
                .body(OkHttpHelper.gson.toJson(newUser))
                .build()
            OkHttpHelper.sendRequest(builder, object : RequestCallback {
                override fun onSuccess(response: Response) {
                    val fromJson = OkHttpHelper.gson.fromJson(response.body?.string(), MyResponse::class.java)
                    if (fromJson.msg == "用户注册成功") {
                        _effect.tryEmit(BindEmailEffect.BindSuccess(newUser.username, newUser.password))
                    } else {
                        setState(
                            currentState.copy(
                                dialog = AuthDialogState(
                                    title = "注册失败",
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

    private fun canBind(email: TextFieldValue, captcha: TextFieldValue): Boolean {
        return email.text.isNotEmpty() && captcha.text.isNotEmpty()
    }

    private fun getCaptcha() {
        val builder = HttpUrlHelper.HttpRequest()
            .post(PlanetApplication.UserIp + "/auth/verification-code/register")
            .body(OkHttpHelper.gson.toJson(Email(currentState.email.text)))
            .build()
        OkHttpHelper.sendRequest(builder, object : RequestCallback {
            override fun onSuccess(response: Response) {
                val fromJson = OkHttpHelper.gson.fromJson(response.body?.string(), MyResponse::class.java)
                if (fromJson.msg == "验证码已发送") {
                    startCountDown()
                } else {
                    _effect.tryEmit(BindEmailEffect.ShowToast("发送失败"))
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
