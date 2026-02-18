package com.creamaker.changli_planet_app.auth.mvi

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.auth.data.remote.dto.Email
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

sealed interface ForgetPasswordIntent {
    data object Initialize : ForgetPasswordIntent
    data class InputEmail(val value: TextFieldValue) : ForgetPasswordIntent
    data class InputCaptcha(val value: TextFieldValue) : ForgetPasswordIntent
    data class InputPassword(val value: TextFieldValue) : ForgetPasswordIntent
    data class InputConfirmPassword(val value: TextFieldValue) : ForgetPasswordIntent
    data object ClickGetCaptcha : ForgetPasswordIntent
    data object ClickChangePassword : ForgetPasswordIntent
    data object ClickBack : ForgetPasswordIntent
    data object DismissDialog : ForgetPasswordIntent
}

sealed interface ForgetPasswordEffect {
    data class ShowToast(val message: String) : ForgetPasswordEffect
    data object ChangePasswordSuccess : ForgetPasswordEffect
}

data class ForgetPasswordUiState(
    val email: TextFieldValue = TextFieldValue(""),
    val captcha: TextFieldValue = TextFieldValue(""),
    val password: TextFieldValue = TextFieldValue(""),
    val confirmPassword: TextFieldValue = TextFieldValue(""),
    val isEnable: Boolean = false,
    val countDown: Int = 0,
    val isCountDown: Boolean = false,
    val isLengthValid: Boolean = false,
    val hasUpperAndLower: Boolean = false,
    val hasNumberAndSpecial: Boolean = false,
    val dialog: AuthDialogState? = null
)

class ForgetPasswordViewModel : BaseMviViewModel<ForgetPasswordIntent, ForgetPasswordUiState>(ForgetPasswordUiState()) {
    private var countDownJob: Job? = null
    private val _effect = MutableSharedFlow<ForgetPasswordEffect>(extraBufferCapacity = 1)
    val effect: SharedFlow<ForgetPasswordEffect> = _effect.asSharedFlow()

    override fun process(intent: ForgetPasswordIntent) {
        when (intent) {
            ForgetPasswordIntent.Initialize -> setState(ForgetPasswordUiState())
            is ForgetPasswordIntent.InputEmail -> {
                setState(currentState.copy(email = intent.value, isEnable = canSubmit(email = intent.value)))
            }
            is ForgetPasswordIntent.InputCaptcha -> {
                val filtered = intent.value.text.filter { it.isDigit() }.take(4)
                val captcha = intent.value.copy(
                    text = filtered,
                    selection = TextRange(
                        intent.value.selection.start.coerceAtMost(filtered.length),
                        intent.value.selection.end.coerceAtMost(filtered.length)
                    )
                )
                setState(currentState.copy(captcha = captcha, isEnable = canSubmit(captcha = captcha)))
            }
            is ForgetPasswordIntent.InputPassword -> {
                val validation = evaluatePassword(intent.value.text)
                setState(
                    currentState.copy(
                        password = intent.value,
                        isLengthValid = validation.isLengthValid,
                        hasUpperAndLower = validation.hasUpperAndLower,
                        hasNumberAndSpecial = validation.hasNumberAndSpecial,
                        isEnable = canSubmit(password = intent.value, isLengthValid = validation.isLengthValid, hasUpperAndLower = validation.hasUpperAndLower, hasNumberAndSpecial = validation.hasNumberAndSpecial)
                    )
                )
            }
            is ForgetPasswordIntent.InputConfirmPassword -> {
                setState(currentState.copy(confirmPassword = intent.value, isEnable = canSubmit(confirmPassword = intent.value)))
            }
            ForgetPasswordIntent.ClickGetCaptcha -> {
                if (currentState.email.text.isNotEmpty() && !currentState.isCountDown) {
                    getCaptcha()
                }
            }
            ForgetPasswordIntent.DismissDialog -> {
                setState(currentState.copy(dialog = null))
            }
            else -> Unit
        }
    }

    fun changeByEmail() {
        viewModelScope.launch {
            val json = ChangePasswordByEmail(
                currentState.email.text,
                currentState.captcha.text,
                currentState.password.text,
                currentState.confirmPassword.text
            )
            val builder = HttpUrlHelper.HttpRequest()
                .put(PlanetApplication.UserIp + "/password/reset")
                .body(OkHttpHelper.gson.toJson(json))
                .build()
            OkHttpHelper.sendRequest(builder, object : RequestCallback {
                override fun onSuccess(response: Response) {
                    val formJson = OkHttpHelper.gson.fromJson(response.body?.string(), MyResponse::class.java)
                    when (formJson.msg) {
                        "用户信息更新成功" -> {
                            _effect.tryEmit(ForgetPasswordEffect.ChangePasswordSuccess)
                        }

                        else -> {
                            setState(
                                currentState.copy(
                                    dialog = AuthDialogState(
                                        title = "更改失败",
                                        message = normalizeDialogMessage(formJson.msg)
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

    override fun onCleared() {
        countDownJob?.cancel()
        super.onCleared()
    }

    private fun getCaptcha() {
        val builder = HttpUrlHelper.HttpRequest()
            .post(PlanetApplication.UserIp + "/auth/verification-code/forget-password")
            .body(OkHttpHelper.gson.toJson(Email(currentState.email.text)))
            .build()
        OkHttpHelper.sendRequest(builder, object : RequestCallback {
            override fun onSuccess(response: Response) {
                val fromJson = OkHttpHelper.gson.fromJson(response.body?.string(), MyResponse::class.java)
                if (fromJson.msg == "验证码已发送") {
                    startCountDown()
                } else {
                    _effect.tryEmit(ForgetPasswordEffect.ShowToast(fromJson.msg))
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

    private fun evaluatePassword(password: String): PasswordValidation {
        val isLengthValid = password.length >= 6
        val hasUpperAndLower = password.matches(".*[A-Z].*".toRegex()) &&
            password.matches(".*[a-z].*".toRegex())
        val hasNumberAndSpecial = password.matches(".*[0-9].*".toRegex()) &&
            password.matches(".*[^A-Za-z0-9].*".toRegex())
        return PasswordValidation(isLengthValid, hasUpperAndLower, hasNumberAndSpecial)
    }

    private fun canSubmit(
        email: TextFieldValue = currentState.email,
        captcha: TextFieldValue = currentState.captcha,
        password: TextFieldValue = currentState.password,
        confirmPassword: TextFieldValue = currentState.confirmPassword,
        isLengthValid: Boolean = currentState.isLengthValid,
        hasUpperAndLower: Boolean = currentState.hasUpperAndLower,
        hasNumberAndSpecial: Boolean = currentState.hasNumberAndSpecial
    ): Boolean {
        return email.text.isNotEmpty() &&
            captcha.text.isNotEmpty() &&
            password.text.isNotEmpty() &&
            confirmPassword.text.isNotEmpty() &&
            isLengthValid &&
            hasUpperAndLower &&
            hasNumberAndSpecial
    }

    private fun normalizeDialogMessage(message: String): String {
        return if (message == "无效的凭证") "账号或密码错误" else message
    }
}

private data class PasswordValidation(
    val isLengthValid: Boolean,
    val hasUpperAndLower: Boolean,
    val hasNumberAndSpecial: Boolean
)

private data class ChangePasswordByEmail(
    val email: String,
    val verification_code: String,
    val new_password: String,
    val confirm_password: String
)
