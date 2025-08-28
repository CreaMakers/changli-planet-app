package com.example.changli_planet_app.Activity

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.pm.PackageInfoCompat
import com.example.changli_planet_app.Activity.Action.LoginAndRegisterAction
import com.example.changli_planet_app.Activity.Action.UserAction
import com.example.changli_planet_app.Activity.Store.LoginAndRegisterStore
import com.example.changli_planet_app.Activity.Store.UserStore
import com.example.changli_planet_app.Core.FullScreenActivity
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.Core.noOpDelegate
import com.example.changli_planet_app.Data.jsonbean.UserPassword
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Utils.Event.FinishEvent
import com.example.changli_planet_app.Widget.Dialog.ExpiredDialog
import com.example.changli_planet_app.databinding.ActivityLoginBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class LoginActivity : FullScreenActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val Login: TextView by lazy { binding.login }
    private val route: TextView by lazy { binding.route }
    private val account: EditText by lazy { binding.account }
    private val password: EditText by lazy { binding.password }
    private val forgetPassword:TextView by lazy { binding.forget }
    private val loginByEmail:TextView by lazy { binding.loginEmail }
    private val iVEye: ImageView by lazy { binding.ivEye }
    private val ivOx: ImageView by lazy { binding.ivOx }
    private val agreementCheckBox: CheckBox by lazy { binding.agreementCheckbox }
    private val disposables by lazy { CompositeDisposable() }
    val store = LoginAndRegisterStore()
    val UserStore=UserStore()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListener()
        checkUpdate()
    }

    private fun checkUpdate(){
        // 检查版本更新
        Looper.myQueue().addIdleHandler {
            val packageManager: PackageManager = this@LoginActivity.packageManager
            val packageInfo: PackageInfo =
                packageManager.getPackageInfo(this@LoginActivity.packageName, 0)
            UserStore.dispatch(
                UserAction.QueryIsLastedApk(
                    this@LoginActivity,
                    PackageInfoCompat.getLongVersionCode(packageInfo),
                    packageInfo.packageName
                )
            )
            false
        }
    }
    private fun initView() {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)// 设置Button的初始状态
        EventBus.getDefault().register(this)
        if (intent.getBooleanExtra("from_token_expired", false)) {
            ExpiredDialog(
                this,
                "您的登录状态过期啦꒰ঌ( ⌯' '⌯)໒꒱",
                "登录提示"
            ).show()
        }
    }

    private fun initListener() {
        disposables.add(
            store.state()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state ->
                    updateButtonState(state.isEnable)
                    updatePasswordVisibility(state.isVisibilityPassword)
                    updateButtonClear(state.isClearPassword)
                }
        )
        store.dispatch(LoginAndRegisterAction.initilaize)
        setUnderLine()
        val accountTextWatcher = object : TextWatcher by noOpDelegate() {
            override fun afterTextChanged(s: Editable?) {
                store.dispatch(
                    LoginAndRegisterAction.InputLogin(
                        account.text.toString(),
                        "account"
                    )
                )
            }
        }
        val passwordTextWatcher = object : TextWatcher by noOpDelegate() {
            override fun afterTextChanged(s: Editable?) {
                store.dispatch(
                    LoginAndRegisterAction.InputLogin(
                        password.text.toString(),
                        "password"
                    )
                )
            }
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
        agreementCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                store.dispatch(LoginAndRegisterAction.InputLogin("checked", "checkbox"))
            } else {
                store.dispatch(LoginAndRegisterAction.InputLogin("unchecked", "checkbox"))
            }

        }
        inputFilter(account)
        inputFilterPassword(password)
        account.setText(intent.getStringExtra("username") ?: "")
        password.setText(intent.getStringExtra("password") ?: "")
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
        getUnderLineScope(route,6,8)
        getUnderLineScope(forgetPassword,0,4)
        getUnderLineScope(loginByEmail,0,4)
        route.setOnClickListener {
            Route.goRegister(this)
        }
        loginByEmail.setOnClickListener{
            Route.goLoginByEmailForcibly(this)
        }
        forgetPassword.setOnClickListener{
            Route.goForgetPassword(this)
        }
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

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onFinish(finishEvent: FinishEvent) {
        if (finishEvent.name.equals("Login")) {
            finish()
        }
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

    private fun getUnderLineScope(view: TextView,start:Int,end:Int){
        var underlinetext = SpannableString(view.text.toString())
        underlinetext.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        view.text = underlinetext
    }

}