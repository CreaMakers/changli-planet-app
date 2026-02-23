package com.creamaker.changli_planet_app.settings.mvi

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.base.mvi.BaseMviViewModel
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.network.HttpUrlHelper
import com.creamaker.changli_planet_app.core.network.MyResponse
import com.creamaker.changli_planet_app.core.network.OkHttpHelper
import com.creamaker.changli_planet_app.core.network.listener.RequestCallback
import com.creamaker.changli_planet_app.utils.EventBusHelper
import com.creamaker.changli_planet_app.utils.event.FinishEvent
import com.creamaker.changli_planet_app.widget.dialog.NormalResponseDialog
import kotlinx.coroutines.launch
import okhttp3.Response

sealed interface AccountSecurityIntent {
    data object Initialize : AccountSecurityIntent
    data class UpdateSafeType(val newPassword: String) : AccountSecurityIntent
    data class ToggleVisibility(val type: String) : AccountSecurityIntent
}

data class AccountSecurityUiState(
    val safeType: Int = 0,
    val curPasswordVisible: Boolean = false,
    val newPasswordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val hasUpperAndLower: Boolean = false,
    val isLengthValid: Boolean = false,
    val hasNumberAndSpecial: Boolean = false
)

class AccountSecurityViewModel : BaseMviViewModel<AccountSecurityIntent, AccountSecurityUiState>(AccountSecurityUiState()) {
    override fun process(intent: AccountSecurityIntent) {
        when (intent) {
            AccountSecurityIntent.Initialize -> setState(AccountSecurityUiState())
            is AccountSecurityIntent.UpdateSafeType -> {
                val password = intent.newPassword
                val isLengthValid = password.length >= 6
                val hasUpperAndLower = password.matches(".*[A-Z].*".toRegex()) &&
                    password.matches(".*[a-z].*".toRegex())
                val hasNumberAndSpecial = password.matches(".*[0-9].*".toRegex()) &&
                    password.matches(".*[^A-Za-z0-9].*".toRegex())

                var safeType = 0
                if (isLengthValid) safeType++
                if (hasUpperAndLower) safeType++
                if (hasNumberAndSpecial) safeType++

                setState(
                    currentState.copy(
                        safeType = safeType,
                        isLengthValid = isLengthValid,
                        hasUpperAndLower = hasUpperAndLower,
                        hasNumberAndSpecial = hasNumberAndSpecial
                    )
                )
            }

            is AccountSecurityIntent.ToggleVisibility -> {
                val newState = when (intent.type) {
                    "curPasswordImg" -> currentState.copy(curPasswordVisible = !currentState.curPasswordVisible)
                    "newPasswordImg" -> currentState.copy(newPasswordVisible = !currentState.newPasswordVisible)
                    "confirmPasswordImg" -> currentState.copy(confirmPasswordVisible = !currentState.confirmPasswordVisible)
                    else -> currentState
                }
                setState(newState)
            }
        }
    }

    fun changePassword(context: Context, oldPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            val httpUrlHelper = HttpUrlHelper.HttpRequest()
                .put(PlanetApplication.UserIp + "/me/password")
                .body(OkHttpHelper.gson.toJson(ChangePasswordJson(oldPassword, newPassword, confirmPassword)))
                .build()

            OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                override fun onSuccess(response: Response) {
                    val fromJson = OkHttpHelper.gson.fromJson(response.body.string(), MyResponse::class.java)
                    when (fromJson.code) {
                        "200" -> EventBusHelper.post(FinishEvent("ChangePassword"))
                        else -> {
                            NormalResponseDialog(
                                context,
                                fromJson.msg,
                                "更改密码失败"
                            ).show()
                        }
                    }
                }

                override fun onFailure(error: String) {
                }
            })
        }
    }
}

data class ChangePasswordJson(
    val old_password: String,
    val new_password: String,
    val confirm_password: String
)
