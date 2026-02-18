package com.creamaker.changli_planet_app.auth.mvi

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.base.mvi.BaseMviViewModel
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

sealed interface RegisterIntent {
    data object Initialize : RegisterIntent
    data class InputAccount(val value: TextFieldValue) : RegisterIntent
    data class InputPassword(val value: TextFieldValue) : RegisterIntent
    data object ClickNext : RegisterIntent
    data object ClickLogin : RegisterIntent
    data object ClickBack : RegisterIntent
}

sealed interface RegisterEffect {
    data class NavigateBindEmail(val username: String, val password: String) : RegisterEffect
    data class ShowToast(val message: String) : RegisterEffect
}

data class RegisterUiState(
    val account: TextFieldValue = TextFieldValue(""),
    val password: TextFieldValue = TextFieldValue(""),
    val canNext: Boolean = false
)

class RegisterViewModel : BaseMviViewModel<RegisterIntent, RegisterUiState>(RegisterUiState()) {
    private val _effect = MutableSharedFlow<RegisterEffect>(extraBufferCapacity = 1)
    val effect: SharedFlow<RegisterEffect> = _effect.asSharedFlow()

    override fun process(intent: RegisterIntent) {
        when (intent) {
            RegisterIntent.Initialize -> {
                setState(RegisterUiState())
            }
            is RegisterIntent.InputAccount -> {
                val filtered = intent.value.text.filter { it.isLetterOrDigit() }
                val account = intent.value.copy(
                    text = filtered,
                    selection = TextRange(
                        intent.value.selection.start.coerceAtMost(filtered.length),
                        intent.value.selection.end.coerceAtMost(filtered.length)
                    )
                )
                setState(currentState.copy(account = account, canNext = canNext(account, currentState.password)))
            }
            is RegisterIntent.InputPassword -> {
                val allowed = Regex("^[a-zA-Z0-9!@#\\$%^&*(),.?\":{}|<>]+$")
                val filtered = intent.value.text.filter { allowed.matches(it.toString()) }
                val password = intent.value.copy(
                    text = filtered,
                    selection = TextRange(
                        intent.value.selection.start.coerceAtMost(filtered.length),
                        intent.value.selection.end.coerceAtMost(filtered.length)
                    )
                )
                setState(currentState.copy(password = password, canNext = canNext(currentState.account, password)))
            }
            else -> Unit
        }
    }

    fun checkName() {
        viewModelScope.launch {
            val s = currentState
            val builder = HttpUrlHelper.HttpRequest()
                .get(PlanetApplication.UserIp + "/availability/{username}")
                .addPathParam("username", s.account.text)
                .build()
            OkHttpHelper.sendRequest(builder, object : RequestCallback {
                override fun onSuccess(response: Response) {
                    val fromJson = OkHttpHelper.gson.fromJson(response.body?.string(), MyResponse::class.java)
                    if (fromJson.msg == "用户验证通过") {
                        _effect.tryEmit(RegisterEffect.NavigateBindEmail(s.account.text, s.password.text))
                    } else if (fromJson.msg == "用户已存在") {
                        _effect.tryEmit(RegisterEffect.ShowToast("账号已被注册，请换一个吧~"))
                    }
                }

                override fun onFailure(error: String) {
                }
            })
        }
    }

    private fun canNext(account: TextFieldValue, password: TextFieldValue): Boolean {
        return account.text.isNotEmpty() && password.text.isNotEmpty() && password.text.length >= 6
    }
}
