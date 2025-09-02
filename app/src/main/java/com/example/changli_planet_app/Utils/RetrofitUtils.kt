package com.example.changli_planet_app.Utils

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.changli_planet_app.Auth.ui.LoginActivity
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Network.Interceptor.NetworkLogger
import com.example.changli_planet_app.Network.OkHttpHelper.AuthInterceptor
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

    //添加公共请求头
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            //配置HTTPDNS解析
            .dns(object : Dns {
                override fun lookup(hostname: String): List<InetAddress> {
                    require(hostname.isNotBlank()) { "hostname can not be null or blank" }
                    return try {
                        // 尝试使用 HTTPDNS 解析
                        val ips = MSDKDnsResolver.getInstance().getAddrByName(hostname)
                        val ipArr = ips.split(";")
                        // 如果没有返回有效的 IP 地址，尝试降级使用 LocalDNS
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
                                        // 忽略无效的 IP
                                    }
                                }
                            }
                            // 如果 HTTPDNS 返回的 IP 列表为空，则降级使用 LocalDNS
                            if (inetAddressList.isEmpty()) {
                                fallbackToLocalDns(hostname)
                            } else {
                                inetAddressList
                            }
                        }
                    } catch (e: Exception) {
                        // 在发生异常时降级使用 LocalDNS
                        fallbackToLocalDns(hostname)
                    }
                }

                // 降级到 LocalDNS 的方法
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
}