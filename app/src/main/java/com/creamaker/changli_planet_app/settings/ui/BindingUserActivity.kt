package com.creamaker.changli_planet_app.settings.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.base.FullScreenActivity
import com.creamaker.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.creamaker.changli_planet_app.common.redux.action.UserAction
import com.creamaker.changli_planet_app.common.redux.store.UserStore
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.databinding.ActivityBindingUserBinding
import com.creamaker.changli_planet_app.utils.event.AppEventBus
import com.creamaker.changli_planet_app.utils.event.FinishEvent
import com.creamaker.changli_planet_app.widget.view.CustomToast
import com.dcelysia.csust_spider.core.RetrofitUtils
import com.example.changli_planet_app.widget.Dialog.SSOWebviewDialog
import com.google.android.material.button.MaterialButton
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * 绑定用户类
 */
class BindingUserActivity : FullScreenActivity<ActivityBindingUserBinding>() {
    private val username: TextView by lazy { binding.etStudentId }
    private val password: TextView by lazy { binding.etStudentPassword }
    private val back: ImageView by lazy { binding.bindingBack }
    private val save: MaterialButton by lazy { binding.saveUser }
    private val webLoginBtn: MaterialButton by lazy { binding.webLoginBtn }

    private val store = UserStore()

    /**
     * 当前是否为"切换学号"场景。该值决定绑定成功后是否清理旧账号的内容缓存 / 过期用户信息。
     */
    private var isSwitchAccount: Boolean = false

    companion object {
        private const val TAG = "BindingUserActivity"
        const val EXTRA_IS_SWITCH_ACCOUNT = "extra_is_switch_account"
    }

    override fun createViewBinding(): ActivityBindingUserBinding =
        ActivityBindingUserBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isSwitchAccount = intent.getBooleanExtra(EXTRA_IS_SWITCH_ACCOUNT, false)
        initView()
        initListener()
        store.dispatch(UserAction.initilaize())
        observeState()
    }

    private fun showLoading() {
        binding.loadingLayout.visibility = View.VISIBLE
        binding.bindingUserLayout.visibility = View.GONE
    }

    fun hideLoading() {
        binding.loadingLayout.visibility = View.GONE
        binding.bindingUserLayout.visibility = View.VISIBLE
    }

    private fun observeState() {
        disposables.add(
            store.state()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state ->
                    if (state.userStats.studentNumber.isNotBlank()) {
                        username.text = state.userStats.studentNumber
                    }
                    Log.d(TAG, "observeState: ${state.uiForLoading}")
                    if (!state.uiForLoading) {
                        hideLoading()
                    }
                }
        )
    }

    private fun initView() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AppEventBus.finishEvent.collect { finishEvent ->
                    if (finishEvent.name == "bindingUser") {
                        if (isSwitchAccount) {
                            PlanetApplication.clearContentCache()
                            UserInfoManager.clearStaleProfile()
                        }
                        showMessage("学号和密码保存成功！")
                        Route.goHomeForcibly(this@BindingUserActivity)
                        finish()
                    }
                }
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                insets.top,
                view.paddingRight,
                view.paddingBottom
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun initListener() {
        save.setOnClickListener { saveUserInfo() }
        back.setOnClickListener { finish() }
        webLoginBtn.setOnClickListener { webLogin() }
    }

    private fun webLogin() {
        save.isEnabled = false
        webLoginBtn.isEnabled = false
        SSOWebviewDialog(
            loginResult = { account, stPassword, loginMode, url, cookies ->
                if (url.isNotEmpty() && cookies.isNotEmpty()) {
                    RetrofitUtils.totalCookieJar.saveFromResponse(url.toHttpUrl(), cookies)
                    CustomToast.showMessage(this, "cookies保存成功！")
                } else {
                    CustomToast.showMessage(this, "cookies保存失败")
                }
                if (account.isNotEmpty() && stPassword.isNotEmpty() && loginMode == "Username") {
                    store.dispatch(UserAction.WebLoginSuccess(this, account, stPassword))
                }
            },
            onDismissCallback = {
                save.isEnabled = true
                webLoginBtn.isEnabled = true
            },
        ).show(supportFragmentManager, "SSOWebDialog")
    }

    private fun saveUserInfo() {
        val studentId = username.text.toString()
        val studentPassword = password.text.toString()
        if (studentId.isEmpty() || studentPassword.isEmpty()) {
            showMessage("学号和密码不能为空")
            return
        }
        store.dispatch(
            UserAction.BindingStudentNumber(
                this,
                studentId,
                studentPassword
            ) { webLogin() }
        )
        showLoading()
    }

    private fun showMessage(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).apply {
            val cardView = CardView(applicationContext).apply {
                radius = 25f
                cardElevation = 8f
                setCardBackgroundColor(getColor(R.color.color_base_white))
                useCompatPadding = true
            }

            val textView = TextView(applicationContext).apply {
                text = message
                textSize = 17f
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
                setPadding(80, 40, 80, 40)
            }

            cardView.addView(textView)
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 140)
            view = cardView
            show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}