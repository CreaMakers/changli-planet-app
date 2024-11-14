package com.example.changli_planet_app.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.SpannedString
import android.text.TextWatcher
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.Data.jsonbean.UserPassword
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.MyResponse
import com.example.changli_planet_app.PlanetApplication
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Route
import com.example.changli_planet_app.UI.LoginInformationDialog
import com.example.changli_planet_app.databinding.ActivityLoginBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    val Login: TextView by lazy { binding.login }
    val route: TextView by lazy { binding.route }
    val account: EditText by lazy { binding.account }
    val password: EditText by lazy { binding.password }
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
        var underlinetext = SpannableString(route.text.toString())
        underlinetext.setSpan(UnderlineSpan(),6,8,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        route.text = underlinetext
        route.setOnClickListener{Route.goRegister(this)}
        // 设置Login Button的初始状态
        Login.isEnabled = false
        // 定义TextWatcher，用于监听account和password EditText内容变化
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // 如果account和password为空，则禁用按钮并设置浅色背景
                Login.isEnabled = !(account.text.isEmpty() || password.text.isEmpty())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        //为account和password EditText添加TextWatcher监听器
        account.addTextChangedListener(textWatcher)
        password.addTextChangedListener(textWatcher)
        inputFilter(account)
        inputFilter(password)
        Login.setOnClickListener{
            val httpUrlHelper = HttpUrlHelper.HttpRequest()
                .post(PlanetApplication.UserIp + "session")
                .header("deviceId",getDeviceId(this))
                .body(OkHttpHelper.gson.toJson(UserPassword(account.text.toString(),password.text.toString())))
                .build()
            OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback{
                override fun onSuccess(response: Response) {
                    var fromJson = OkHttpHelper.gson.fromJson(response.body?.string(), MyResponse::class.java)
                    when(fromJson.msg){
                        "用户登录成功"->{
                            runOnUiThread {
                                Toast.makeText(this@LoginActivity,"账号或密码错误",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                override fun onFailure(error: String) {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity,"网络请求异常",Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
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
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}