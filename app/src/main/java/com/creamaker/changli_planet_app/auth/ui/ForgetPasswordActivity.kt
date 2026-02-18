package com.creamaker.changli_planet_app.auth.ui

import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.creamaker.changli_planet_app.auth.mvi.ForgetPasswordEffect
import com.creamaker.changli_planet_app.auth.mvi.ForgetPasswordIntent
import com.creamaker.changli_planet_app.auth.mvi.ForgetPasswordUiState
import com.creamaker.changli_planet_app.auth.mvi.ForgetPasswordViewModel
import com.creamaker.changli_planet_app.base.ComposeActivity
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.core.theme.AppTheme
import com.creamaker.changli_planet_app.utils.EventBusHelper
import com.creamaker.changli_planet_app.utils.event.FinishEvent
import com.creamaker.changli_planet_app.widget.view.CustomToast
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ForgetPasswordActivity : ComposeActivity() {
    private val viewModel by lazy { ViewModelProvider(this)[ForgetPasswordViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        viewModel.process(ForgetPasswordIntent.Initialize)
        observeEffects()

        setComposeContent {
            val state by viewModel.state.collectAsState()
            ForgetPasswordScreen(
                state = state,
                onIntent = { intent ->
                    when (intent) {
                        ForgetPasswordIntent.ClickChangePassword -> viewModel.changeByEmail()
                        ForgetPasswordIntent.ClickGetCaptcha -> viewModel.process(intent)
                        ForgetPasswordIntent.ClickBack -> finish()
                        else -> viewModel.process(intent)
                    }
                }
            )
        }
    }

    private fun observeEffects() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is ForgetPasswordEffect.ShowToast -> {
                            CustomToast.showMessage(this@ForgetPasswordActivity, effect.message)
                        }

                        ForgetPasswordEffect.ChangePasswordSuccess -> {
                            CustomToast.showMessage(this@ForgetPasswordActivity, "密码更改成功")
                            Route.goLoginForcibly(this@ForgetPasswordActivity)
                            EventBusHelper.post(FinishEvent("changePasswordByEmail"))
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onFinish(finishEvent: FinishEvent) {
        if (finishEvent.name == "changePasswordByEmail") {
            finish()
        }
    }
}

@Composable
private fun ForgetPasswordScreen(
    state: ForgetPasswordUiState,
    onIntent: (ForgetPasswordIntent) -> Unit
) {
    AuthScaffold {
        AuthTopBar(onBack = { onIntent(ForgetPasswordIntent.ClickBack) })
        AuthSpacer(20.dp)

        AuthHero(
            title = "找回密码",
            subtitle = "通过邮箱验证码重置"
        )
        AuthSpacer(22.dp)

        AuthSection {
            AuthInput(
                value = state.email,
                onValueChange = { onIntent(ForgetPasswordIntent.InputEmail(it)) },
                label = "邮箱"
            )

            AuthCaptchaRow(
                value = state.captcha,
                onValueChange = { onIntent(ForgetPasswordIntent.InputCaptcha(it)) },
                sendText = if (state.isCountDown && state.countDown > 0) "${state.countDown}s" else "获取验证码",
                sendEnabled = !state.isCountDown,
                onSendClick = { onIntent(ForgetPasswordIntent.ClickGetCaptcha) }
            )

            AuthInput(
                value = state.password,
                onValueChange = { onIntent(ForgetPasswordIntent.InputPassword(it)) },
                label = "新密码",
                visualTransformation = PasswordVisualTransformation()
            )

            AuthInput(
                value = state.confirmPassword,
                onValueChange = { onIntent(ForgetPasswordIntent.InputConfirmPassword(it)) },
                label = "确认密码",
                visualTransformation = PasswordVisualTransformation()
            )

            PasswordHintRow(label = "至少 6 位", passed = state.isLengthValid)
            PasswordHintRow(label = "包含大小写字母", passed = state.hasUpperAndLower)
            PasswordHintRow(label = "包含数字和特殊字符", passed = state.hasNumberAndSpecial)
        }

        AuthSpacer(16.dp)
        AuthPrimaryButton(
            text = "更改密码",
            enabled = state.isEnable,
            onClick = { onIntent(ForgetPasswordIntent.ClickChangePassword) }
        )
    }

    state.dialog?.let { dialog ->
        AuthInformationDialog(
            title = dialog.title,
            message = dialog.message,
            onDismiss = { onIntent(ForgetPasswordIntent.DismissDialog) }
        )
    }
}

@Composable
private fun PasswordHintRow(label: String, passed: Boolean) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (passed) "✔" else "✖",
            color = if (passed) colors.successGreenColor else colors.errorRedColor,
            fontSize = 13.sp,
            modifier = Modifier.width(18.dp)
        )
        Text(
            text = label,
            color = colors.primaryTextColor,
            fontSize = 13.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgetPasswordPreview() {
    AppSkinTheme {
        ForgetPasswordScreen(
            state = ForgetPasswordUiState(
                email = TextFieldValue("demo@changli.app"),
                captcha = TextFieldValue("9527"),
                password = TextFieldValue("Aa123456!"),
                confirmPassword = TextFieldValue("Aa123456!"),
                isEnable = true,
                isLengthValid = true,
                hasUpperAndLower = true,
                hasNumberAndSpecial = true
            ),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true, name = "ForgetPassword Weak")
@Composable
private fun ForgetPasswordWeakPreview() {
    AppSkinTheme {
        ForgetPasswordScreen(
            state = ForgetPasswordUiState(
                email = TextFieldValue(""),
                captcha = TextFieldValue(""),
                password = TextFieldValue("abc"),
                confirmPassword = TextFieldValue("abc"),
                isEnable = false,
                isLengthValid = false,
                hasUpperAndLower = false,
                hasNumberAndSpecial = false
            ),
            onIntent = {}
        )
    }
}
