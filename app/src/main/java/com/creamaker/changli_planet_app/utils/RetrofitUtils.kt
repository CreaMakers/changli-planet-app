package com.creamaker.changli_planet_app.utils

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.creamaker.changli_planet_app.auth.ui.LoginActivity
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.network.OkHttpHelper.AuthInterceptor
import com.creamaker.changli_planet_app.core.network.interceptor.NetworkLogger
import com.creamaker.changli_planet_app.feature.mooc.cookie.PersistentCookieJar
import com.tencent.msdk.dns.MSDKDnsResolver
import okhttp3.Dns
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object RetrofitUtils {
    private const val FreshNewsIp = "http://113.44.47.220:8085/app/"
    private const val UserIp = "http://113.44.47.220:8083/app/users/"
    private const val IpLocation ="http://ip-api.com/json/"
    private const val MOOC_LOCATION = "http://pt.csust.edu.cn"
    private const val SSO_AUTH_URL = "https://authserver.csust.edu.cn"
    private const val SSO_EHALL_URL = "https://ehall.csust.edu.cn"

    //添加公共请求头 - 用于需要认证的 API
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            //配置HTTPDNS解析
            .dns(object : Dns {
                override fun lookup(hostname: String): List<InetAddress> {
                    require(hostname.isNotBlank()) { "hostname can not be null or blank" }
                    return try {
                        val ips = MSDKDnsResolver.getInstance().getAddrByName(hostname)
                        val ipArr = ips.split(";")
                        if (ipArr.isEmpty() || ipArr.all { it == "0" }) {
                            fallbackToLocalDns(hostname)
                        } else {
                            val inetAddressList = mutableListOf<InetAddress>()
                            for (ip in ipArr) {
                                if (ip != "0") {
                                    try {
                                        Log.d("MyIp", ip)
                                        inetAddressList.add(InetAddress.getByName(ip))
                                    } catch (ignored: UnknownHostException) {
                                    }
                                }
                            }
                            if (inetAddressList.isEmpty()) {
                                fallbackToLocalDns(hostname)
                            } else {
                                inetAddressList
                            }
                        }
                    } catch (e: Exception) {
                        fallbackToLocalDns(hostname)
                    }
                }

                private fun fallbackToLocalDns(hostname: String): List<InetAddress> {
                    return try {
                        InetAddress.getAllByName(hostname).toList()
                    } catch (e: UnknownHostException) {
                        emptyList()
                    }
                }
            })
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(NetworkLogger.getLoggingInterceptor())
            .addInterceptor(AuthInterceptor(object : AuthInterceptor.TokenExpiredHandler {
                override fun onTokenExpired() {
                    Handler(Looper.getMainLooper()).post {
                        val intent = Intent(PlanetApplication.appContext, LoginActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra("from_token_expired", true)
                        PlanetApplication.appContext.startActivity(intent)
                    }
                }
            }))
            .build()
    }

    // MOOC 和 SSO 专用客户端 - 不包含 AuthInterceptor，添加 Cookie 支持
    private val moocClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)  // MOOC 系统可能较慢，增加超时时间
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(NetworkLogger.getLoggingInterceptor())
            .cookieJar(PersistentCookieJar())
            .build()
    }

    val instanceNewFresh: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(FreshNewsIp)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val instanceUser: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(UserIp)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val instanceIP: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(IpLocation)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val instanceMooc: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(MOOC_LOCATION)
            .client(moocClient)
            .addConverterFactory(retrofit2.converter.scalars.ScalarsConverterFactory.create())  // 支持 String 响应
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val instanceSSOAuth: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(SSO_AUTH_URL)
            .client(moocClient)
            .addConverterFactory(retrofit2.converter.scalars.ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val instanceSSOEhall: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(SSO_EHALL_URL)
            .client(moocClient)
            .addConverterFactory(retrofit2.converter.scalars.ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}