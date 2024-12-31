package com.example.changli_planet_app.Activity

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.MotionEvent
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.Activity.Action.LoginAndRegisterAction
import com.example.changli_planet_app.Activity.Store.LoginAndRegisterStore
import com.example.changli_planet_app.Data.jsonbean.UserPassword
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.Util.Event.FinishEvent
import com.example.changli_planet_app.databinding.ActivityLoginBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.Subscribe

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val Login: TextView by lazy { binding.login }
    private val route: TextView by lazy { binding.route }
    private val account: EditText by lazy { binding.account }
    private val password: EditText by lazy { binding.password }
    private val iVEye: ImageView by lazy { binding.ivEye }
    private val ivOx: ImageView by lazy { binding.ivOx }
    private val agreementCheckBox: CheckBox by lazy { binding.agreementCheckbox }

    val store = LoginAndRegisterStore()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)// 设置Button的初始状态
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        store.state()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { state ->
                updateButtonState(state.isEnable)
                updatePasswordVisibility(state.isVisibilityPassword)
                updateButtonClear(state.isClearPassword)
            }

        store.dispatch(LoginAndRegisterAction.initilaize)
        setUnderLine()
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

        Login.setOnClickListener {
            store.dispatch(
                LoginAndRegisterAction.Login
                    (
                    UserPassword(account.text.toString(), password.text.toString()),
                    this
                )
            )
        }
        ivOx.setOnClickListener {
            if (store.currentState.isClearPassword) {
                clearCurPassword()
            }
        }
        iVEye.setOnClickListener {
            store.dispatch(LoginAndRegisterAction.ChangeVisibilityOfPassword)
        }
        account.addTextChangedListener(accountTextWatcher)
        password.addTextChangedListener(passwordTextWatcher)
        agreementCheckBox.setOnCheckedChangeListener { _ , isChecked ->
            if (isChecked) {
                store.dispatch(LoginAndRegisterAction.input("checked", "checkbox"))
            } else {
                store.dispatch(LoginAndRegisterAction.input("unchecked", "checkbox"))
            }

        }
        inputFilter(account)
        inputFilter(password)
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

    private fun setUnderLine() {
        var underlinetext = SpannableString(route.text.toString())
        underlinetext.setSpan(UnderlineSpan(), 6, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        route.text = underlinetext
        route.setOnClickListener { Route.goRegister(this) }
    }

    private fun clearCurPassword() {
        password.setText("")
    }

    private fun updateButtonState(isEnable: Boolean) {
        Login.isEnabled = isEnable
        if (isEnable) {
            Login.setBackgroundResource(R.drawable.enable_button)
        } else {
            Login.setBackgroundResource(R.drawable.disable_button)
        }
    }

    private fun updateButtonClear(isClearPassword: Boolean) {
        if (!isClearPassword) {
            ivOx.setImageResource(R.drawable.dialog_login)
        } else {
            ivOx.setImageResource(R.drawable.login_ox_24)
        }
    }

    private fun updatePasswordVisibility(isVisible: Boolean) {
        if (isVisible) {
            password.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            iVEye.setImageResource(R.drawable.login_visibiliby_eyes)
        } else {
            password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            iVEye.setImageResource(R.drawable.line_invisible2)
        }
        password.setSelection(password.text.length)
    }


    @Subscribe
    fun onFinish(finishEvent: FinishEvent) {
        if (finishEvent.name == "Login") {
            finish()
        }
    }

    companion object {
        public fun getDeviceId(context: Context): String {
            return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }
    }
}