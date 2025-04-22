package com.example.changli_planet_app.Activity

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.widget.EditText
import android.widget.TextView
import com.example.changli_planet_app.Activity.Action.LoginAndRegisterAction
import com.example.changli_planet_app.Activity.Store.LoginAndRegisterStore
import com.example.changli_planet_app.Core.FullScreenActivity
import com.example.changli_planet_app.Data.jsonbean.UserPassword
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.Core.noOpDelegate
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Utils.Event.FinishEvent
import com.example.changli_planet_app.databinding.ActivityRegisterBinding
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class RegisterActivity : FullScreenActivity() {
    lateinit var binding: ActivityRegisterBinding
    val register: TextView by lazy { binding.register }
    val route: TextView by lazy { binding.routes }
    val account: EditText by lazy { binding.account }
    val password: EditText by lazy { binding.password }
    val store = LoginAndRegisterStore()
    private val disposables by lazy { CompositeDisposable() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
                    binding.apply {
                        lengthIcon.setImageResource(if (state.isLengthValid) R.drawable.dui else R.drawable.cuo)
                        upperLowerIcon.setImageResource(if (state.hasUpperAndLower) R.drawable.dui else R.drawable.cuo)
                        numberSpecialIcon.setImageResource(if (state.hasNumberAndSpecial) R.drawable.dui else R.drawable.cuo)
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
        register.setOnClickListener {
            store.dispatch(
                LoginAndRegisterAction.Login
                    (
                    UserPassword(account.text.toString(), password.text.toString()),
                    this
                )
            )
        }
        account.addTextChangedListener(accountTextWatcher)
        password.addTextChangedListener(passwordTextWatcher)
        inputFilter(account)
        inputFilterPassword(password)
        register.setOnClickListener {
            store.dispatch(
                LoginAndRegisterAction.Register
                    (
                    UserPassword(account.text.toString(), password.text.toString()),
                    this
                )
            )
        }
    }

    private fun validatePassword(password: String) {
        val isLengthValid = password.length >= 8
        val hasUpperAndLower = password.matches(".*[A-Z].*".toRegex()) &&
                password.matches(".*[a-z].*".toRegex())

        val hasNumberAndSpecial = password.matches(".*[0-9].*".toRegex()) &&
                password.matches(".*[^A-Za-z0-9].*".toRegex())
        binding.apply {
            lengthIcon.setImageResource(if (isLengthValid) R.drawable.dui else R.drawable.cuo)
            upperLowerIcon.setImageResource(if (hasUpperAndLower) R.drawable.dui else R.drawable.cuo)
            numberSpecialIcon.setImageResource(if (hasNumberAndSpecial) R.drawable.dui else R.drawable.cuo)
        }
        store.dispatch(
            LoginAndRegisterAction.input(
                if (isLengthValid && hasUpperAndLower && hasNumberAndSpecial) "valid" else "invalid",
                "password"
            )
        )
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