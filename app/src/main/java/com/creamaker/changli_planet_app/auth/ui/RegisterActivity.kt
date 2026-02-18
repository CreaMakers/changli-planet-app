package com.creamaker.changli_planet_app.auth.ui

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.creamaker.changli_planet_app.auth.mvi.RegisterEffect
import com.creamaker.changli_planet_app.auth.mvi.RegisterIntent
import com.creamaker.changli_planet_app.auth.mvi.RegisterUiState
import com.creamaker.changli_planet_app.auth.mvi.RegisterViewModel
import com.creamaker.changli_planet_app.base.ComposeActivity
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.utils.event.FinishEvent
import com.creamaker.changli_planet_app.widget.view.CustomToast
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class RegisterActivity : ComposeActivity() {
    private val viewModel by lazy { ViewModelProvider(this)[RegisterViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        viewModel.process(RegisterIntent.Initialize)
        observeEffects()

        setComposeContent {
            val state by viewModel.state.collectAsState()
            RegisterScreen(
                state = state,
                onIntent = { intent ->
                    when (intent) {
                        RegisterIntent.ClickNext -> viewModel.checkName()
                        RegisterIntent.ClickLogin -> Route.goLoginForcibly(this)
                        RegisterIntent.ClickBack -> finish()
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
                        is RegisterEffect.NavigateBindEmail -> {
                            Route.goBindEmailFromRegister(
                                this@RegisterActivity,
                                effect.username,
                                effect.password
                            )
                        }

                        is RegisterEffect.ShowToast -> {
                            CustomToast.showMessage(this@RegisterActivity, effect.message)
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
        if (finishEvent.name == "Register") {
            finish()
        }
    }
}

@Composable
private fun RegisterScreen(
    state: RegisterUiState,
    onIntent: (RegisterIntent) -> Unit
) {
    AuthScaffold {
        AuthTopBar(onBack = { onIntent(RegisterIntent.ClickBack) })
        AuthSpacer(20.dp)

        AuthHero(
            title = "创建账户",
            subtitle = "只需两步，马上开始"
        )
        AuthSpacer(22.dp)

        AuthSection {
            AuthInput(
                value = state.account,
                onValueChange = { onIntent(RegisterIntent.InputAccount(it)) },
                label = "账号"
            )

            AuthInput(
                value = state.password,
                onValueChange = { onIntent(RegisterIntent.InputPassword(it)) },
                label = "密码",
                visualTransformation = PasswordVisualTransformation()
            )
        }

        AuthSpacer()
        AuthPrimaryButton(
            text = "下一步",
            enabled = state.canNext,
            onClick = { onIntent(RegisterIntent.ClickNext) }
        )

        AuthSpacer(12.dp)
        AuthLink(
            text = "已有账号？去登录",
            onClick = { onIntent(RegisterIntent.ClickLogin) }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    AppSkinTheme {
        RegisterScreen(
            state = RegisterUiState(
                account = TextFieldValue("new_user"),
                password = TextFieldValue("Aa123456!"),
                canNext = true
            ),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true, name = "Register Disabled")
@Composable
private fun RegisterScreenDisabledPreview() {
    AppSkinTheme {
        RegisterScreen(
            state = RegisterUiState(
                account = TextFieldValue(""),
                password = TextFieldValue(""),
                canNext = false
            ),
            onIntent = {}
        )
    }
}
