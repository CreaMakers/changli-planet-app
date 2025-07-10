package com.example.changli_planet_app.Activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.changli_planet_app.Cache.UserInfoManager
import com.example.changli_planet_app.Core.FullScreenActivity
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.Data.jsonbean.UserPassword
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.MyResponse
import com.gradle.scan.plugin.internal.dep.io.netty.util.internal.StringUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
        PlanetApplication.deviceId = LoginActivity.getDeviceId(this)
        // 使用协程来处理延迟任务
        lifecycleScope.launch {
            if (StringUtil.isNullOrEmpty(PlanetApplication.accessToken)) {
                delay(300) // 延迟 0.2 秒
                Route.goLogin(this@SplashActivity)
            } else {
                delay(200) // 延迟 0.2 秒
                Route.goHome(this@SplashActivity)
            }
            finish()
        }
    }

}
