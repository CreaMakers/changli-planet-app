package com.dcelysia.csust_spider.core

import android.R.attr.action
import android.util.Log
import com.dcelysia.csust_spider.education.data.remote.EducationData
import com.dcelysia.csust_spider.education.data.remote.services.AuthService
import com.dcelysia.csust_spider.mooc.data.remote.repository.MoocRepository
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody

//统一认证及教务登录拦截器
//对response进行判断，一旦发现处于cookie过期状态就重新登录刷新cookie
//判断条件：mmkv中存在cookie但是返回的html却是登录页面，就进行登录重试
//如果重试后仍然不行就要抛出异常
class NetworkRetryInterceptor(
    private val mmkv: MMKV,
    private val key: String
) : Interceptor {

    private val TAG = "NetworkRetryInterceptor"

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val originalResponse = chain.proceed(request)
        val responseBodyString = originalResponse.body?.string().orEmpty()
        Log.d(TAG,responseBodyString.contains("用户登录").toString())
        Log.d(TAG, MMKVUtil.hasNonEmptyString(mmkv,key).toString())
        Log.d(TAG, MMKVUtil.hasKey(mmkv,key).toString())
        Log.d(TAG, MMKVUtil.hasAnyKey(mmkv).toString())
        // 判断是否跳转到登录页（cookie 过期）
        if (responseBodyString.contains("用户登录") && MMKVUtil.hasAnyKey(mmkv)) {
            Log.d(TAG, "检测到登录页面，cookie 可能过期，开始自动登录流程...")

            // 阻塞登录流程（放在IO线程）
            val reloginSuccess = runBlocking(Dispatchers.IO) {
                try {
                    Log.d(TAG,"网络库得到的账号密码：${EducationData.studentId},${EducationData.studentPassword}")
                    mmkv.clearAll()
                    val ssoResult = MoocRepository.instance
                        .login(EducationData.studentId, EducationData.studentPassword)
                        .filter { it !is Resource.Loading }
                        .firstOrNull()

                    if (ssoResult is Resource.Success) {
                        Log.d(TAG, "SSO 登录成功")
                        val eduSuccess = AuthService.Login(
                            EducationData.studentId,
                            EducationData.studentPassword
                        )
                        Log.d(TAG, "教务登录结果: $eduSuccess")
                        eduSuccess
                    } else false
                } catch (e: Exception) {
                    Log.e(TAG, "登录重试异常: ${e.message}")
                    false
                }
            }

            if (reloginSuccess) {
                Log.d(TAG, "重新登录成功，重试原始请求...")
                val retryResponse = chain.proceed(request)
                val retryBodyString = retryResponse.body?.string().orEmpty()
            //重建响应体
                return retryResponse.newBuilder()
                    .body(retryBodyString.toResponseBody(retryResponse.body?.contentType()))
                    .build()
            }
        }

        // 未触发重试或重试失败
        return originalResponse.newBuilder()
            .body(responseBodyString.toResponseBody(originalResponse.body?.contentType()))
            .build()
    }
}
