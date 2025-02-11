package com.example.changli_planet_app.Activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.changli_planet_app.Cache.UserInfoManager
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.Data.jsonbean.UserPassword
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.MyResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response

class SplashActivity : AppCompatActivity() {
    private val username: String by lazy { UserInfoManager.username }
    private val password: String by lazy { UserInfoManager.userPassword }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        PlanetApplication.deviceId = LoginActivity.getDeviceId(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 使用协程来处理延迟任务
        lifecycleScope.launch {
            delay(200) // 延迟 0.2 秒
            Route.goHome(this@SplashActivity)
            finish()
//            autoLogin()
        }
    }

    private suspend fun autoLogin() {
        try {
            if (username.isEmpty() || password.isEmpty()) {
                Route.goLogin(this@SplashActivity)
                finish()
                return
            }
            withContext(Dispatchers.IO) {
                val userPassword = UserPassword(username, password)
                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .post(PlanetApplication.UserIp + "/session")
                    .header("deviceId", LoginActivity.getDeviceId(this@SplashActivity))
                    .body(OkHttpHelper.gson.toJson(userPassword))
                    .build()
                OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        var fromJson = OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            MyResponse::class.java
                        )
                        when (fromJson.msg) {
                            "用户登录成功" -> {
                                PlanetApplication.accessToken =
                                    response.header("Authorization", "") ?: ""
                                Route.goHome(this@SplashActivity)
                                finish()
                            }

                            else -> {
                                Route.goLogin(this@SplashActivity)
                                finish()
                            }
                        }
                    }

                    override fun onFailure(error: String) {
                        Route.goLogin(this@SplashActivity)
                        finish()
                    }

                })
            }

        } catch (e: Exception) {
            Route.goLogin(this@SplashActivity)
            finish()
        }
    }
}
