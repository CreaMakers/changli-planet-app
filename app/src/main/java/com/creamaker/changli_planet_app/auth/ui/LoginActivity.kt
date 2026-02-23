package com.creamaker.changli_planet_app.auth.ui

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.auth.mvi.LoginEffect
import com.creamaker.changli_planet_app.auth.mvi.LoginIntent
import com.creamaker.changli_planet_app.auth.mvi.LoginUiState
import com.creamaker.changli_planet_app.auth.mvi.LoginViewModel
import com.creamaker.changli_planet_app.base.ComposeActivity
import com.creamaker.changli_planet_app.common.redux.action.UserAction
import com.creamaker.changli_planet_app.common.redux.store.UserStore
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.utils.EventBusHelper
import com.creamaker.changli_planet_app.utils.event.FinishEvent
import com.creamaker.changli_planet_app.widget.dialog.ExpiredDialog
import kotlinx.coroutines.launch

class LoginActivity : ComposeActivity() {
    private val userStore = UserStore()
    private val viewModel by lazy {
        ViewModelProvider(this)[LoginViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialAccount = intent.getStringExtra("username") ?: ""
        val initialPassword = intent.getStringExtra("password") ?: ""
        viewModel.process(LoginIntent.Initialize(initialAccount, initialPassword))

        if (intent.getBooleanExtra("from_token_expired", false)) {
            ExpiredDialog(this, "您的登录状态过期啦꒰ঌ( ⌯' '⌯)໒꒱", "登录提示").show()
        }

        observeEffects()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Route.goHome(this@LoginActivity)
                finish()
            }
        })

        setComposeContent {
            val state by viewModel.state.collectAsState()
            LoginScreen(
                state = state,
                onIntent = { intent ->
                    when (intent) {
                        LoginIntent.ClickLogin -> viewModel.login()
                        LoginIntent.ClickRegister -> Route.goRegister(this)
                        LoginIntent.ClickTourist -> {
                            PlanetApplication.is_tourist = true
                            Route.goHome(this)
                        }
                        LoginIntent.ClickForgetPassword -> Route.goForgetPassword(this)
                        LoginIntent.ClickLoginByEmail -> Route.goLoginByEmail(this)
                        LoginIntent.ClickHint -> {
                            ExpiredDialog(
                                this,
                                "由于账号的注册与登录功能主要服务于“长理星球新鲜事”板块，但目前尚未完善，您可以点击右下角的「跳过，稍后登录」以游客身份体验应用的其他功能（除了新鲜事以外其他的基础功能都可用哦(*￣3￣)╭~)",
                                "账号相关"
                            ).show()
                        }
                        LoginIntent.ClickBack -> finish()
                        else -> viewModel.process(intent)
                    }
                }
            )
        }

        checkUpdate()
    }

    private fun observeEffects() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        LoginEffect.LoginSuccess -> {
                            Route.goHomeForcibly(this@LoginActivity)
                            EventBusHelper.post(FinishEvent("Login"))
                            EventBusHelper.post(FinishEvent("LoginByEmail"))
                        }
                    }
                }
            }
        }
    }

    private fun checkUpdate() {
        Looper.myQueue().addIdleHandler {
            val packageManager: PackageManager = this@LoginActivity.packageManager
            val packageInfo: PackageInfo =
                packageManager.getPackageInfo(this@LoginActivity.packageName, 0)
            userStore.dispatch(
                UserAction.QueryIsLastedApk(
                    this@LoginActivity,
                    PackageInfoCompat.getLongVersionCode(packageInfo),
                    packageInfo.packageName
                )
            )
            false
        }
    }
}

@Composable
private fun LoginScreen(
    state: LoginUiState,
    onIntent: (LoginIntent) -> Unit
) {
    AuthScaffold {
        AuthSpacer(10.dp)

        AuthHero(
            title = "登录",
            subtitle = "欢迎回来，继续探索你的长理星球"
        )
        AuthSpacer(68.dp)

        AuthSection {
            AuthInput(
                value = state.account,
                onValueChange = { onIntent(LoginIntent.InputAccount(it)) },
                label = "账号"
            )
            AuthInput(
                value = state.password,
                onValueChange = { onIntent(LoginIntent.InputPassword(it)) },
                label = "密码",
                visualTransformation = if (state.isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailing = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        IconButton(
                            onClick = { onIntent(LoginIntent.TogglePasswordVisible) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (state.isPasswordVisible) {
                                        R.drawable.ic_login_visibility_eyes
                                    } else {
                                        R.drawable.ic_line_invisible2
                                    }
                                ),
                                contentDescription = "密码可见性",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        if (state.canClearPassword) {
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { onIntent(LoginIntent.ClearPassword) },
                                modifier = Modifier
                                    .size(32.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_login_ox_24),
                                    contentDescription = "清空密码",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            )

            AuthAgreement(
                checked = state.checked,
                onCheckedChange = { onIntent(LoginIntent.CheckAgreement(it)) }
            )
        }

        AuthSpacer(35.dp)
        AuthPrimaryButton(
            text = "登录",
            enabled = state.canLogin,
            onClick = { onIntent(LoginIntent.ClickLogin) }
        )

        AuthSpacer(35.dp)
        AuthTwoLinks(
            leftText = "忘记密码",
            rightText = "邮箱登录",
            onLeft = { onIntent(LoginIntent.ClickForgetPassword) },
            onRight = { onIntent(LoginIntent.ClickLoginByEmail) }
        )

        AuthSpacer(16.dp)
        AuthTwoLinks(
            leftText = "没有账号？去注册",
            rightText = "跳过，稍后登录",
            onLeft = { onIntent(LoginIntent.ClickRegister) },
            onRight = { onIntent(LoginIntent.ClickTourist) }
        )
    }

    state.dialog?.let { dialog ->
        AuthInformationDialog(
            title = dialog.title,
            message = dialog.message,
            onDismiss = { onIntent(LoginIntent.DismissDialog) }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    AppSkinTheme {
        LoginScreen(
            state = LoginUiState(
                account = TextFieldValue("changli_user"),
                password = TextFieldValue("Aa1234567"),
                checked = true,
                canLogin = true,
                isPasswordVisible = false,
                canClearPassword = true
            ),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true, name = "Login Empty")
@Composable
private fun LoginScreenEmptyPreview() {
    AppSkinTheme {
        LoginScreen(
            state = LoginUiState(),
            onIntent = {}
        )
    }
}
