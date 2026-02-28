package com.creamaker.changli_planet_app.auth.mvi

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.auth.data.remote.dto.UserPassword
import com.creamaker.changli_planet_app.base.mvi.BaseMviViewModel
import com.creamaker.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.network.HttpUrlHelper
import com.creamaker.changli_planet_app.core.network.MyResponse
import com.creamaker.changli_planet_app.core.network.OkHttpHelper
import com.creamaker.changli_planet_app.core.network.listener.RequestCallback
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.Response

sealed interface LoginIntent {
    data class Initialize(val account: String, val password: String) : LoginIntent
    data class InputAccount(val value: TextFieldValue) : LoginIntent
    data class InputPassword(val value: TextFieldValue) : LoginIntent
    data class CheckAgreement(val checked: Boolean) : LoginIntent
    data object TogglePasswordVisible : LoginIntent
    data object ClearPassword : LoginIntent
    data object ClickLogin : LoginIntent
    data object ClickRegister : LoginIntent
    data object ClickTourist : LoginIntent
    data object ClickForgetPassword : LoginIntent
    data object ClickLoginByEmail : LoginIntent
    data object ClickHint : LoginIntent
    data object ClickBack : LoginIntent
    data object DismissDialog : LoginIntent
}

sealed interface LoginEffect {
    data object LoginSuccess : LoginEffect
}

data class LoginUiState(
    val account: TextFieldValue = TextFieldValue(""),
    val password: TextFieldValue = TextFieldValue(""),
    val checked: Boolean = false,
    val canLogin: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val canClearPassword: Boolean = false,
    val dialog: AuthDialogState? = null
)

class LoginViewModel : BaseMviViewModel<LoginIntent, LoginUiState>(LoginUiState()) {
    private val _effect = MutableSharedFlow<LoginEffect>(extraBufferCapacity = 1)
    val effect: SharedFlow<LoginEffect> = _effect.asSharedFlow()

    override fun process(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.Initialize -> {
                setState(LoginUiState())
                if (intent.account.isNotEmpty()) process(LoginIntent.InputAccount(TextFieldValue(intent.account)))
                if (intent.password.isNotEmpty()) process(LoginIntent.InputPassword(TextFieldValue(intent.password)))
            }
            is LoginIntent.InputAccount -> {
                val account = filterAccount(intent.value)
                setState(currentState.copy(account = account, canLogin = canLogin(account, currentState.password, currentState.checked)))
            }
            is LoginIntent.InputPassword -> {
                val password = filterPassword(intent.value)
                setState(
                    currentState.copy(
                        password = password,
                        canLogin = canLogin(currentState.account, password, currentState.checked),
                        canClearPassword = password.text.isNotEmpty()
                    )
                )
            }
            is LoginIntent.CheckAgreement -> {
                setState(
                    currentState.copy(
                        checked = intent.checked,
                        canLogin = canLogin(currentState.account, currentState.password, intent.checked)
                    )
                )
            }
            LoginIntent.TogglePasswordVisible -> {
                setState(currentState.copy(isPasswordVisible = !currentState.isPasswordVisible))
            }
            LoginIntent.ClearPassword -> {
                val empty = TextFieldValue("")
                setState(
                    currentState.copy(
                        password = empty,
                        canLogin = canLogin(currentState.account, empty, currentState.checked),
                        canClearPassword = false
                    )
                )
            }
            LoginIntent.DismissDialog -> {
                setState(currentState.copy(dialog = null))
            }
            else -> Unit
        }
    }

    fun login() {
        viewModelScope.launch {
            val current = currentState
            val request = UserPassword(current.account.text, current.password.text)
            val httpUrlHelper = HttpUrlHelper.HttpRequest()
                .post(PlanetApplication.UserIp + "/sessions/password")
                .header("deviceId", PlanetApplication.getSystemDeviceId())
                .body(OkHttpHelper.gson.toJson(request))
                .build()
            OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                override fun onSuccess(response: Response) {
                    val fromJson = OkHttpHelper.gson.fromJson(response.body.string(), MyResponse::class.java)
                    when (fromJson.msg) {
                        "用户登录成功" -> {
                            PlanetApplication.isExpired = false
                            UserInfoManager.username = request.username
                            UserInfoManager.userPassword = request.password
                            PlanetApplication.accessToken = response.header("Authorization", "") ?: ""
                            _effect.tryEmit(LoginEffect.LoginSuccess)
                        }

                        else -> {
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
                }

                override fun onFailure(error: String) {
                }
            })
        }
    }

    private fun filterAccount(raw: TextFieldValue): TextFieldValue {
        val filtered = raw.text.filter { it.isLetterOrDigit() }
        val selection = TextRange(
            raw.selection.start.coerceAtMost(filtered.length),
            raw.selection.end.coerceAtMost(filtered.length)
        )
        return raw.copy(text = filtered, selection = selection)
    }

    private fun filterPassword(raw: TextFieldValue): TextFieldValue {
        val specialChars = "!@#$%^&*(),.?\":{}|<>，。？「」+-\\]"
        val filtered = raw.text.filter { char ->
            char.isLetterOrDigit() || specialChars.contains(char)
        }
        val selection = TextRange(
            raw.selection.start.coerceAtMost(filtered.length),
            raw.selection.end.coerceAtMost(filtered.length)
        )
        return raw.copy(text = filtered, selection = selection)
    }

    private fun canLogin(account: TextFieldValue, password: TextFieldValue, checked: Boolean): Boolean {
        return account.text.isNotEmpty() && password.text.isNotEmpty() && checked
    }

    private fun normalizeDialogMessage(message: String): String {
        return if (message == "无效的凭证") "账号或密码错误" else message
    }
}
