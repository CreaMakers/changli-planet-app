package com.creamaker.changli_planet_app.auth.ui

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.widget.EditText
import android.widget.TextView
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.auth.redux.action.LoginAndRegisterAction
import com.creamaker.changli_planet_app.auth.redux.state.LoginAndRegisterState
import com.creamaker.changli_planet_app.auth.redux.store.LoginAndRegisterStore
import com.creamaker.changli_planet_app.base.FullScreenActivity
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.core.noOpDelegate
import com.creamaker.changli_planet_app.databinding.ActivityRegisterBinding
import com.creamaker.changli_planet_app.utils.Event.FinishEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * 注册页面
 */
class RegisterActivity : FullScreenActivity<ActivityRegisterBinding>() {
    val register: TextView by lazy { binding.register }
    val route: TextView by lazy { binding.routes }
    val account: EditText by lazy { binding.account }
    val password: EditText by lazy { binding.password }
    val store = LoginAndRegisterStore()

    override fun createViewBinding(): ActivityRegisterBinding = ActivityRegisterBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposables.add(
            store.state()
                .subscribe { state ->
                    if (!state.isEnable) {
                        register.isEnabled = state.isEnable
                        register.setBackgroundResource(R.drawable.disable_button)
                    } else {
                        register.isEnabled = state.isEnable
                        register.setBackgroundResource(R.drawable.enable_button)
                    }
                }

        )

        EventBus.getDefault().register(this)
        store.dispatch(LoginAndRegisterAction.initilaize)
        store.dispatch(LoginAndRegisterAction.input("checked", "checkbox"))
        setUnderLine()
        // 定义TextWatcher，用于监听account和password EditText内容变化
        val accountTextWatcher = object : TextWatcher by noOpDelegate() {
            private var isUpdating = false

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                isUpdating = true
                store.dispatch(LoginAndRegisterAction.input(account.text.toString(), "account"))
                isUpdating = false
            }
        }
        val passwordTextWatcher = object : TextWatcher by noOpDelegate() {
            private var isUpdating = false

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                isUpdating = true
                store.dispatch(LoginAndRegisterAction.input(password.text.toString(), "password"))
                isUpdating = false
            }
        }
//        register.setOnClickListener {
//            store.dispatch(
//                LoginAndRegisterAction.Login
//                    (
//                    UserPassword(account.text.toString(), password.text.toString()),
//                    this
//                )
//            )
//        }
        account.addTextChangedListener(accountTextWatcher)
        password.addTextChangedListener(passwordTextWatcher)
        inputFilter(account)
        inputFilterPassword(password)
        register.setOnClickListener {
            store.dispatch(LoginAndRegisterAction.CheckName(this,account.text.toString(),password.text.toString()))
        }
    }


    private fun checkNameFlat(state: LoginAndRegisterState){
//        if(state.checkNameFlat==1){
//            Route.goBindEmailFromRegister(this,account.text.toString(), password.text.toString())
//        }
//        else if(state.checkNameFlat==-1){
//            CustomToast.showMessage(this,"账号已被注册，请换一个吧~")
//            store.dispatch(LoginAndRegisterAction.input(0.toString(),"checkNameFlat")) //恢复默认状态，防止一直发消息
//        }
    }
    private fun setUnderLine() {
        var underlinetext = SpannableString(route.text.toString())
        underlinetext.setSpan(UnderlineSpan(), 6, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        route.text = underlinetext
        route.setOnClickListener { Route.goLoginForcibly(this) }
    }

    private fun inputFilter(editText: EditText) {
        val filter = InputFilter { source, _, _, _, _, _ ->
            val regex = Regex("^[a-zA-Z0-9]*$")
            if (source.isEmpty() || regex.matches(source)) {
                null // 允许输入（包括删除操作）
            } else {
                "" // 拒绝非法字符
            }
        }
        editText.filters = arrayOf(filter)
    }

    private fun inputFilterPassword(editText: EditText) {
        val filter = InputFilter { source, _, _, _, _, _ ->
            // 允许：英文、数字、指定特殊字符，禁止中文和全角符号
            val regex = Regex("^[a-zA-Z0-9!@#\\$%\\^&*(),.?\":{}|<>]+$")

            if (source.isEmpty()) {
                null // 允许删除操作
            } else if (regex.matches(source)) {
                null // 允许合法字符
            } else {
                // 过滤掉非法字符（包括中文）
                val filtered = source.toString().filter { char ->
                    regex.matches(char.toString())
                }
                filtered.ifEmpty { "" } // 如果全被过滤，返回空字符串
            }
        }
        editText.filters = arrayOf(filter)

        val watcher = object : TextWatcher by noOpDelegate() {
            override fun afterTextChanged(s: Editable?) {
                val length = s?.length ?: 0
                if (length < 6) {
                    editText.error = "密码至少需要6个字符"
                } else {
                    editText.error = null
                }
            }
        }
        editText.addTextChangedListener(watcher)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        disposables.clear()
    }

    @Subscribe
    fun onFinish(finishEvent: FinishEvent) {
        if (finishEvent.name.equals("Register")) {
            finish()
        }
    }
}