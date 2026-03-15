package com.creamaker.changli_planet_app.auth.ui

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.creamaker.changli_planet_app.auth.mvi.LoginByEmailEffect
import com.creamaker.changli_planet_app.auth.mvi.LoginByEmailIntent
import com.creamaker.changli_planet_app.auth.mvi.LoginByEmailUiState
import com.creamaker.changli_planet_app.auth.mvi.LoginByEmailViewModel
import com.creamaker.changli_planet_app.base.ComposeActivity
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.utils.EventBusHelper
import com.creamaker.changli_planet_app.utils.event.FinishEvent
import com.creamaker.changli_planet_app.widget.view.CustomToast
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class LoginByEmailActivity : ComposeActivity() {
    private val viewModel by lazy {
        ViewModelProvider(this)[LoginByEmailViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        viewModel.process(LoginByEmailIntent.Initialize)
        observeEffects()

        setComposeContent {
            val state by viewModel.state.collectAsState()
            LoginByEmailScreen(
                state = state,
                onIntent = { intent ->
                    when (intent) {
                        LoginByEmailIntent.ClickLogin -> viewModel.loginByEmail()
                        LoginByEmailIntent.ClickGetCaptcha -> viewModel.process(intent)
                        LoginByEmailIntent.ClickAccountLogin -> Route.goLoginForcibly(this)
                        LoginByEmailIntent.ClickForgetPassword -> Route.goForgetPassword(this)
                        LoginByEmailIntent.ClickRegister -> Route.goRegister(this)
                        LoginByEmailIntent.ClickBack -> finish()
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
                        LoginByEmailEffect.LoginSuccess -> {
                            Route.goHomeForcibly(this@LoginByEmailActivity)
                            EventBusHelper.post(FinishEvent("LoginByEmail"))
                            EventBusHelper.post(FinishEvent("Login"))
                        }

                        is LoginByEmailEffect.ShowToast -> {
                            CustomToast.showMessage(this@LoginByEmailActivity, effect.message)
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
        if (finishEvent.name == "LoginByEmail") {
            finish()
        }
    }
}

@Composable
private fun LoginByEmailScreen(
    state: LoginByEmailUiState,
    onIntent: (LoginByEmailIntent) -> Unit
) {
    AuthScaffold {
        AuthTopBar(onBack = { onIntent(LoginByEmailIntent.ClickBack) })
        AuthSpacer(20.dp)
        AuthHero(
            title = "邮箱登录",
            subtitle = "使用验证码更快捷"
        )
        AuthSpacer(22.dp)

        AuthSection {
            AuthInput(
                value = state.email,
                onValueChange = { onIntent(LoginByEmailIntent.InputEmail(it)) },
                label = "邮箱"
            )

            AuthCaptchaRow(
                value = state.captcha,
                onValueChange = { onIntent(LoginByEmailIntent.InputCaptcha(it)) },
                sendText = if (state.isCountDown && state.countDown > 0) "${state.countDown}s" else "获取验证码",
                sendEnabled = !state.isCountDown,
                onSendClick = { onIntent(LoginByEmailIntent.ClickGetCaptcha) }
            )

            AuthAgreement(
                checked = state.checked,
                onCheckedChange = { onIntent(LoginByEmailIntent.CheckAgreement(it)) }
            )
        }

        AuthSpacer()
        AuthPrimaryButton(
            text = "登录",
            enabled = state.canLogin,
            onClick = { onIntent(LoginByEmailIntent.ClickLogin) }
        )

        AuthSpacer(16.dp)
        AuthTwoLinks(
            leftText = "忘记密码",
            rightText = "账户登录",
            onLeft = { onIntent(LoginByEmailIntent.ClickForgetPassword) },
            onRight = { onIntent(LoginByEmailIntent.ClickAccountLogin) }
        )

        AuthSpacer(12.dp)
        AuthLink(
            text = "没有账号？去注册",
            onClick = { onIntent(LoginByEmailIntent.ClickRegister) }
        )
    }

    state.dialog?.let { dialog ->
        AuthInformationDialog(
            title = dialog.title,
            message = dialog.message,
            onDismiss = { onIntent(LoginByEmailIntent.DismissDialog) }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginByEmailPreview() {
    AppSkinTheme {
        LoginByEmailScreen(
            state = LoginByEmailUiState(
                email = TextFieldValue("demo@changli.app"),
                captcha = TextFieldValue("1234"),
                checked = true,
                canLogin = true,
                countDown = 30,
                isCountDown = true
            ),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true, name = "LoginByEmail Idle")
@Composable
private fun LoginByEmailIdlePreview() {
    AppSkinTheme {
        LoginByEmailScreen(
            state = LoginByEmailUiState(
                email = TextFieldValue(""),
                captcha = TextFieldValue(""),
                checked = false,
                canLogin = false,
                countDown = 0,
                isCountDown = false
            ),
            onIntent = {}
        )
    }
}
