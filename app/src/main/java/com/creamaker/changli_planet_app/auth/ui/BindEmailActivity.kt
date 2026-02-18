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
import com.creamaker.changli_planet_app.auth.mvi.BindEmailEffect
import com.creamaker.changli_planet_app.auth.mvi.BindEmailIntent
import com.creamaker.changli_planet_app.auth.mvi.BindEmailUiState
import com.creamaker.changli_planet_app.auth.mvi.BindEmailViewModel
import com.creamaker.changli_planet_app.base.ComposeActivity
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.utils.EventBusHelper
import com.creamaker.changli_planet_app.utils.event.FinishEvent
import com.creamaker.changli_planet_app.widget.view.CustomToast
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class BindEmailActivity : ComposeActivity() {
    private val viewModel by lazy { ViewModelProvider(this)[BindEmailViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)

        val account = intent.getStringExtra("username") ?: ""
        val password = intent.getStringExtra("password") ?: ""
        viewModel.process(BindEmailIntent.Initialize(account, password))
        observeEffects()

        setComposeContent {
            val state by viewModel.state.collectAsState()
            BindEmailScreen(
                state = state,
                onBack = { finish() },
                onIntent = { intent ->
                    when (intent) {
                        BindEmailIntent.ClickBind -> viewModel.bind()
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
                        is BindEmailEffect.BindSuccess -> {
                            Route.goLoginFromRegister(
                                this@BindEmailActivity,
                                effect.username,
                                effect.password
                            )
                            EventBusHelper.post(FinishEvent("Register"))
                            EventBusHelper.post(FinishEvent("bindEmail"))
                        }

                        is BindEmailEffect.ShowToast -> {
                            CustomToast.showMessage(this@BindEmailActivity, effect.message)
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
        if (finishEvent.name == "bindEmail") {
            finish()
        }
    }
}

@Composable
private fun BindEmailScreen(
    state: BindEmailUiState,
    onBack: () -> Unit,
    onIntent: (BindEmailIntent) -> Unit
) {
    AuthScaffold {
        AuthTopBar(onBack = onBack)
        AuthSpacer(20.dp)
        AuthHero(
            title = "绑定邮箱",
            subtitle = "用于接收验证码和保障账号安全"
        )
        AuthSpacer(22.dp)

        AuthSection {
            AuthInput(
                value = state.email,
                onValueChange = { onIntent(BindEmailIntent.InputEmail(it)) },
                label = "邮箱"
            )

            AuthCaptchaRow(
                value = state.captcha,
                onValueChange = { onIntent(BindEmailIntent.InputCaptcha(it)) },
                sendText = if (state.isCountDown && state.countDown > 0) "${state.countDown}s" else "获取验证码",
                sendEnabled = !state.isCountDown,
                onSendClick = { onIntent(BindEmailIntent.ClickGetCaptcha) }
            )
        }

        AuthSpacer()
        AuthPrimaryButton(
            text = "绑定并登录",
            enabled = state.canBind,
            onClick = { onIntent(BindEmailIntent.ClickBind) }
        )
    }

    state.dialog?.let { dialog ->
        AuthInformationDialog(
            title = dialog.title,
            message = dialog.message,
            onDismiss = { onIntent(BindEmailIntent.DismissDialog) }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BindEmailPreview() {
    AppSkinTheme {
        BindEmailScreen(
            state = BindEmailUiState(
                email = TextFieldValue("new@changli.app"),
                captcha = TextFieldValue("1234"),
                canBind = true,
                isCountDown = false,
                countDown = 0
            ),
            onBack = {},
            onIntent = {}
        )
    }
}

@Preview(showBackground = true, name = "BindEmail Countdown")
@Composable
private fun BindEmailCountDownPreview() {
    AppSkinTheme {
        BindEmailScreen(
            state = BindEmailUiState(
                email = TextFieldValue("new@changli.app"),
                captcha = TextFieldValue(""),
                canBind = false,
                isCountDown = true,
                countDown = 18
            ),
            onBack = {},
            onIntent = {}
        )
    }
}
