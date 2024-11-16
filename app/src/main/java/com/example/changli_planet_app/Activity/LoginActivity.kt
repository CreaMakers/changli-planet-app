package com.example.changli_planet_app.Activity

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.Activity.Action.LoginAction
import com.example.changli_planet_app.Activity.Store.LoginStore
import com.example.changli_planet_app.Data.jsonbean.UserPassword
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.MyResponse
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.UI.LoginInformationDialog
import com.example.changli_planet_app.databinding.ActivityLoginBinding
import okhttp3.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    val Login: TextView by lazy { binding.login }
    val route: TextView by lazy { binding.route }
    val account: EditText by lazy { binding.account }
    val password: EditText by lazy { binding.password }
    val store = LoginStore()
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
            .subscribe{state->
                if (!state.isEnable){
                    Login.setBackgroundColor(Color.parseColor("#8E959F"))
                }else{
                    Login.setBackgroundResource(R.drawable.enable_button)
                }
            }
        store.dispatch(LoginAction.initilaize)
        setUnderLine()
        // 定义TextWatcher，用于监听account和password EditText内容变化
        val accountTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {store.dispatch(LoginAction.input(account.text.toString(),"account"))}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        val passwordTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {store.dispatch(LoginAction.input(password.text.toString(),"password"))}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        Login.setOnClickListener{store.dispatch(LoginAction.Login
            (UserPassword(account.text.toString(),password.text.toString()),
            this))
        }
        account.addTextChangedListener(accountTextWatcher)
        password.addTextChangedListener(passwordTextWatcher)
        inputFilter(account)
        inputFilter(password)
    }
    private fun inputFilter(editText: EditText){
        val inputFilter = InputFilter { source, _, _, _, _, _ ->
            // 允许的字符是英文字母和数字
            val regex = Regex("^[a-zA-Z0-9]*$")
            // 如果输入内容符合正则表达式，则允许输入，否则返回空字符串禁止输入
            if (regex.matches(source)) source else ""
        }
        editText.filters = arrayOf(inputFilter)
    }
    private fun setUnderLine(){
        var underlinetext = SpannableString(route.text.toString())
        underlinetext.setSpan(UnderlineSpan(),6,8,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        route.text = underlinetext
        route.setOnClickListener{ Route.goRegister(this)}
    }
    companion object {
        public fun getDeviceId(context: Context): String {
            return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }
    }
}