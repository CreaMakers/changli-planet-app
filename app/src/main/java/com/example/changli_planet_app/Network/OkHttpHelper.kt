package com.example.changli_planet_app.Network

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.changli_planet_app.Activity.LoginActivity
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Network.Interceptor.NetworkLogger
import com.example.changli_planet_app.Network.Response.NormalResponse
import com.example.changli_planet_app.Utils.PlanetConst
import com.google.gson.Gson
import com.tencent.msdk.dns.MSDKDnsResolver
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import okio.GzipSink
import okio.buffer
import java.io.File
import java.io.IOException
import java.lang.reflect.Type
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object OkHttpHelper {

    private val TAG = "OkHttpHelper"
    class AuthInterceptor(private val tokenExpiredHandler: TokenExpiredHandler? = null) :
        Interceptor {
        companion object {
            private const val MAX_RETRY_ATTEMPTS = 2
            private const val MAX_BACKOFF_DELAY = 200L // 最大延迟0.2秒
        }

        interface TokenExpiredHandler {
            fun onTokenExpired()
        }

        private var retryCount = 0


        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()

            // 添加 Authorization 头
            val requestWithToken = originalRequest.newBuilder()
                .addHeader("Authorization", PlanetApplication.accessToken ?: "")
                .addHeader("deviceId", PlanetApplication.deviceId)
                .build()

            if (originalRequest.url.encodedPath == "/app/users/me/token" && originalRequest.method == "PUT") {
                return chain.proceed(requestWithToken)
            }

            var response = chain.proceed(requestWithToken)
            val responseBody = response.peekBody(Long.MAX_VALUE).string()
            Log.d(TAG, responseBody)
            val realResponse = gson.fromJson(responseBody, NormalResponse::class.java)
            if (realResponse?.code == "401" &&
                realResponse?.msg == PlanetConst.UNAUTHORIZATION &&
                retryCount < MAX_RETRY_ATTEMPTS
            ) {
                retryCount++
                synchronized(this) {
                    try {
                        // 计算当前重试的延迟时间（指数退避）
                        val delayMs =
                            (30L * (1 shl (retryCount - 1))).coerceAtMost(MAX_BACKOFF_DELAY)
                        Thread.sleep(delayMs)

                        // 刷新 Token
                        val newToken = refreshTokenSync()
                        if (newToken != null) {
                            response.close()
                            // 使用新 Token 重试请求
                            val newRequest = originalRequest.newBuilder()
                                .addHeader("Authorization", newToken)
                                .addHeader("deviceId", PlanetApplication.deviceId)
                                .build()
                            return chain.proceed(newRequest)
                        } else {
                        }
                    } catch (e: Exception) {
                        Log.e("Token Refresh", "Retry attempt $retryCount failed", e)
                    }
                }
            }
            // 如果重试次数达到上限，可以触发重新登录
            if (retryCount >= MAX_RETRY_ATTEMPTS) {
                Log.w("Token Refresh", "Max retry attempts reached")
                // 清除token
                PlanetApplication.clearCacheAll()
                // 处理token过期操作
                tokenExpiredHandler?.onTokenExpired()
            }
            retryCount = 0
            return response
        }
    }

    // 设置缓存
    // 懒加载OkhttpClient
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

    fun sendRequest(httpUrlHelper: HttpUrlHelper, callback: RequestCallback) {
        val requestBuilder = Request.Builder().url(httpUrlHelper.buildUrl())
        // 添加头部信息
        for ((key, value) in httpUrlHelper.headers) {
            requestBuilder.addHeader(key, value)
        }

        // 创建请求
        val request = when (httpUrlHelper.requestType) {
            HttpUrlHelper.RequestType.GET -> requestBuilder.get().build()
            HttpUrlHelper.RequestType.POST -> {
                if (httpUrlHelper.fileParams.isNotEmpty() || httpUrlHelper.formParams.isNotEmpty()) {
                    val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
                    httpUrlHelper.formParams.forEach { key, value ->
                        multipartBuilder.addFormDataPart(key, value)
                    }
                    httpUrlHelper.fileParams.forEach { key, value ->
                        val (file, mediaType) = value
                        val requestBody = file.asRequestBody(mediaType)
                        multipartBuilder.addFormDataPart(
                            key,
                            file.name,
                            requestBody
                        )
                    }
                    requestBuilder.post(multipartBuilder.build())
                } else {
                    requestBuilder.post(
                        (httpUrlHelper.requestBody
                            ?: "").toRequestBody("application/json".toMediaTypeOrNull())
                    )
                }
                requestBuilder.build()
            }

            HttpUrlHelper.RequestType.PUT -> {
                requestBuilder.put(
                    (httpUrlHelper.requestBody
                        ?: "").toRequestBody("application/json".toMediaTypeOrNull())
                )
                    .build()
            }

            HttpUrlHelper.RequestType.DELETE -> {
                requestBuilder.delete(
                    (httpUrlHelper.requestBody
                        ?: "").toRequestBody("application/json".toMediaTypeOrNull())
                )
                    .build()
            }
        }
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure("error")
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                callback.onSuccess(response)
            }
        })
    }

    /**
     * 刷新AccessToken
     */
    private fun refreshTokenSync(): String? {
        try {
            val request = Request.Builder()
                .url("${PlanetApplication.UserIp}/me/token")
                .addHeader("Authorization", PlanetApplication.accessToken ?: "")
                .addHeader("deviceId", PlanetApplication.deviceId)
                .put("".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
                .build()

            return client.newCall(request).execute().use { response ->
                when {
                    response.isSuccessful -> {
                        response.headers["Authorization"]?.also { newToken ->
                            PlanetApplication.accessToken = newToken
                            Log.d("Token Refresh", "Token refreshed successfully")
                        }
                    }

                    else -> {
                        Log.w("Token Refresh", "Failed to refresh token: ${response.code}")
                        val errorBody = response.body?.string()
                        Log.w("Token Refresh", "Error response: $errorBody")
                    }
                }
                response.headers["Authorization"]
            }
        } catch (e: Exception) {
            Log.e("Token Refresh", "Network error during token refresh", e)
            return null
        }
    }

//    /**
//     * 刷新AccessToken和RefreshToken
//     */
//    private fun refreshAccessToken() {
//        val json = gson.toJson(PlanetApplication.accessToken)
//        val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
//        val request = Request.Builder()
//            .url(PlanetApplication.UserIp + "/me/token")
//            .put(body)
//            .build()
//        // 发送请求
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Log.e("API Error", "请求失败: ${e.message}")
//                // 这里可以处理失败逻辑，比如重试或用户提示
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                if (response.isSuccessful && response.body != null) {
//                    PlanetApplication.accessToken = response.headers["Authorization"]
//                } else {
//                    Log.e("API Error", "响应错误: ${response.code} - ${response.message}")
//                    // 处理响应不成功的逻辑
//                }
//            }
//        })
//    }

    //解析返回的Json和发送的Json
    val gson: Gson by lazy { Gson() }

    /**
     * Gzip请求体内部类，用于构建被Gzip压缩的Body
     */
    class GzipRequestBody(private val requestBody: RequestBody) : RequestBody() {
        override fun contentType(): MediaType? {
            //返回原Body的MiMe
            return requestBody.contentType()
        }

        override fun writeTo(sink: BufferedSink) {
            //GzipSink压缩流用于压缩数据
            val gzipSink = GzipSink(sink).buffer()
            //将Body给写入GzipSink压缩
            requestBody.writeTo(gzipSink)
            //关闭压缩流
            gzipSink.close()
        }

    }

    /**
     * @param description : 文件的描述
     * @param url : 请求的url
     * @param file : 文件
     * @param responseType : 返回值的类型
     * @param onSuccess : 成功回调
     * @param onFailure : 失败回调
     */
    // 上传 Gzip 压缩后的文件，并附加一个字符串参数
    fun <T> uploadGzipFile(
        description: String,
        url: String,
        file: File,
        responseType: Type,
        onSuccess: (T) -> Unit,
        onFailure: (String) -> Unit
    ) {
        // 创建一个 RequestBody，包装原始文件的数据
        val fileBody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
        // 使用 GzipRequestBody 包装原始的 RequestBody，确保文件在上传时进行 Gzip 压缩
        val gzipBody = GzipRequestBody(fileBody)
        // 构建 MultipartBody，用于上传文件、字符串参数和其他表单字段
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM) // 设置表单类型
            .addFormDataPart("file", file.name, gzipBody) // 添加文件字段，文件将以 Gzip 压缩的形式上传
            .addFormDataPart("description", description) // 添加文件描述
            .build()
        // 构建 HTTP 请求，指定目标 URL 和请求体
        val request = Request.Builder()
            .url(PlanetApplication.UserIp + url) // 指定上传文件的服务器 URL
            .addHeader("Content-Encoding", "gzip")
            .post(requestBody) // 使用 POST 方法上传数据
            .build()
        // 使用 OkHttpClient 异步发送请求，避免阻塞主线程
        client.newCall(request).enqueue(object : Callback {
            // 处理请求失败的情况
            override fun onFailure(call: Call, e: IOException) {
                onFailure(e.message ?: "Unknown Error") // 调用失败回调并传递错误信息
            }

            // 处理服务器的响应
            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    // 将响应体转换为字符串
                    val json = responseBody.string()
                    try {
                        // 解析 JSON 响应到指定的泛型类型 T
                        val parsedObject: T = gson.fromJson(json, responseType)
                        // 成功时，调用 onSuccess 回调并传递解析后的对象
                        onSuccess(parsedObject)
                    } catch (e: Exception) {
                        // 如果解析失败，调用 onFailure 回调并传递错误信息
                        onFailure("Failed to parse response: ${e.message}")
                    }
                } ?: onFailure("Response body is null") // 如果响应体为空，调用 onFailure 回调
            }
        })
    }

    /**
     * @param url : WebSocket 服务器的 URL
     * @param onOpen : WebSocket 连接成功回调
     * @param onMessage : 收到消息时的回调
     * @param onFailure : 连接失败或发生错误时的回调
     * @param onClosing : 连接关闭时的回调
     * @param onClosed : 连接完全关闭时的回调
     */
    fun connectWebSocket(
        url: String,
        onOpen: (WebSocket) -> Unit,
        onMessage: (String) -> Unit,
        onFailure: (String) -> Unit,
        onClosing: (Int, String) -> Unit,
        onClosed: (Int, String) -> Unit
    ) {
        // 构建 WebSocket 请求
        val request = Request.Builder()
            .url(PlanetApplication.UserIp + url)
            .build()
        // 创建 WebSocket 并添加事件监听器
        client.newWebSocket(request, object : WebSocketListener() {
            // WebSocket 连接成功
            override fun onOpen(webSocket: WebSocket, response: Response) {
                onOpen(webSocket) // 连接成功时回调
            }

            // 收到服务器发送的消息
            override fun onMessage(webSocket: WebSocket, text: String) {
                onMessage(text) // 收到消息时回调
            }

            // WebSocket 连接出错
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                onFailure(t.message ?: "Unknown Error") // 出错时回调
            }

            // WebSocket 连接关闭的处理
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                onClosing(code, reason) // 连接关闭时回调
                webSocket.close(code, reason) // 主动关闭 WebSocket 连接
            }

            // WebSocket 连接已关闭
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                onClosed(code, reason) // 连接完全关闭时回调
            }
        })
    }

    /**
     * 关闭 WebSocket
     * @param webSocket : WebSocket 对象
     * @param code : 关闭码
     * @param reason : 关闭原因
     */
    fun closeWebSocket(webSocket: WebSocket, code: Int = 1000, reason: String = "Normal closure") {
        webSocket.close(code, reason)
    }

    /**
     * 利用OkHttp的连接复用池，预连接以提高网络请求效率
     * 初步构想闪屏页面预请求首页数据
     */
    fun preRequest(url: String) {
        val startTime = System.currentTimeMillis()
        val request = Request.Builder()
            .url(url)
            .head()
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                //预连接成功与否不需关心
            }

            override fun onFailure(call: Call, e: IOException) {
                //预连接成功与否不需关心
            }
        })
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        Log.d("MyTag", "PreRequestTime:${duration}")
    }
}

