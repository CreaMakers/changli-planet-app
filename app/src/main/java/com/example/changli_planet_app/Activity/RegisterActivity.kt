package com.example.changli_planet_app.Activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.Activity.Action.LoginAndRegisterAction
import com.example.changli_planet_app.Activity.Store.LoginAndRegisterStore
import com.example.changli_planet_app.Data.jsonbean.UserPassword
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.MyResponse
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.R
import com.example.changli_planet_app.UI.LoginInformationDialog
import com.example.changli_planet_app.Util.Event.FinishEvent
import com.example.changli_planet_app.databinding.ActivityRegisterBinding
import com.tencent.mmkv.MMKV
import io.reactivex.rxjava3.disposables.CompositeDisposable
import okhttp3.Response
import org.greenrobot.eventbus.Subscribe

class RegisterActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegisterBinding
    val register: TextView by lazy { binding.register }
    val route: TextView by lazy { binding.routes }
    val account: EditText by lazy { binding.account }
    val mmkv = MMKV.defaultMMKV()
    val password: EditText by lazy { binding.password }
    val store = LoginAndRegisterStore()
    private val disposables by lazy { CompositeDisposable() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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


        store.dispatch(LoginAndRegisterAction.initilaize)
        store.dispatch(LoginAndRegisterAction.input("checked", "checkbox"))
        setUnderLine()
        // 定义TextWatcher，用于监听account和password EditText内容变化
        val accountTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                store.dispatch(LoginAndRegisterAction.input(account.text.toString(), "account"))
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        val passwordTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                store.dispatch(LoginAndRegisterAction.input(password.text.toString(), "password"))
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
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
        route.setOnClickListener { Route.goRegister(this) }
    }

    private fun inputFilter(editText: EditText) {
        val inputFilter = InputFilter { source, _, _, _, _, _ ->
            // 允许的字符是英文字母和数字
            val regex = Regex("^[a-zA-Z0-9]*$")
            // 如果输入内容符合正则表达式，则允许输入，否则返回空字符串禁止输入
            if (regex.matches(source)) source else ""
        }
        editText.filters = arrayOf(inputFilter)
    }

    private fun inputFilterPassword(editText: EditText) {
        val inputFilter = InputFilter { source, _, _, _, _, _ ->
            val regex = Regex("^[a-zA-Z0-9!@#\$%^&*(),.?\":{}|<>]+$")
            source.toString().filter { char ->
                regex.matches(char.toString())
            }
        }
        editText.filters = arrayOf(inputFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    @Subscribe
    fun onFinish(finishEvent: FinishEvent) {
        if (finishEvent.name == "Register") {
            finish()
        }
    }
}