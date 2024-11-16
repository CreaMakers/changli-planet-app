package com.example.changli_planet_app.Activity
import android.content.Intent
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
import com.example.changli_planet_app.Data.jsonbean.UserPassword
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.MyResponse
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.R
import com.example.changli_planet_app.UI.LoginInformationDialog
import com.example.changli_planet_app.Util.Event.FinishEvent
import com.example.changli_planet_app.databinding.ActivityRegisterBinding
import com.tencent.mmkv.MMKV
import okhttp3.Response
import org.greenrobot.eventbus.Subscribe

class RegisterActivity : AppCompatActivity() {
    lateinit var binding : ActivityRegisterBinding
    val register:TextView by lazy { binding.register }
    val route: TextView by lazy { binding.routes }
    val account: EditText by lazy { binding.account }
    val mmkv = MMKV.defaultMMKV()
    val password: EditText by lazy { binding.password }
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
        var underlinetext = SpannableString(route.text.toString())
        underlinetext.setSpan(UnderlineSpan(),6,8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        underlinetext.setSpan(object : ClickableSpan(){
            override fun onClick(widget: View) {
                val intent = Intent(this@RegisterActivity,RegisterActivity::class.java)
                startActivity(intent)
                finish()
            }
        },6,8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        route.text = underlinetext
        // 设置Login Button的初始状态
        register.isEnabled = false
        // 定义TextWatcher，用于监听account和password EditText内容变化
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // 如果account和password为空，则禁用按钮并设置浅色背景
                register.isEnabled = !(account.text.isEmpty() || password.text.isEmpty())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        //为account和password EditText添加TextWatcher监听器
        account.addTextChangedListener(textWatcher)
        password.addTextChangedListener(textWatcher)
        inputFilter(account)
        inputFilter(password)
        register.setOnClickListener{}
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
    @Subscribe
    fun onFinish(finishEvent: FinishEvent){
        if(finishEvent.name=="Register"){
            finish()
        }
    }
}