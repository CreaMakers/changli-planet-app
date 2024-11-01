package com.example.changli_planet_app.Activity

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    val Login: Button by lazy { binding.login }
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
        // 设置Login Button的初始状态
        Login.isEnabled = false
        // 定义TextWatcher，用于监听account和password EditText内容变化
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // 如果account和password为空，则禁用按钮并设置浅色背景
                if (account.text.isEmpty() || password.text.isEmpty()) {
                    Login.isEnabled = false
                } else {
                    // 否则启用按钮并恢复正常背景颜色
                    Login.isEnabled = true
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        //为account和password EditText添加TextWatcher监听器
        account.addTextChangedListener(textWatcher)
        password.addTextChangedListener(textWatcher)
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
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}