package com.dcelysia.csust_spider.mooc.cookie

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.map
import kotlin.compareTo


class PersistentCookieJar private constructor(): CookieJar {
    private val gson = Gson()

    // 内存缓存：存不可变 List，合并时整体替换，避免并发修改
    private val memoryCache = ConcurrentHashMap<String, List<Cookie>>()
    var scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val pendingJobs = ConcurrentHashMap<String, Job>()
    private val saveDelayMs = 500L
    companion object{
        private val TAG = "PersistentCookieJar"
        private val MMKV_ID = "csust_cookie_jar"
        val instance by lazy { PersistentCookieJar() }

        fun initialize(context: Context) {
            try {
                MMKV.initialize(context)
            } catch (t: Throwable) {
                Log.w(TAG, "MMKV.initialize failed", t)
            }
        }
    }

    private val mmkv by lazy {
        try {
            MMKV.mmkvWithID(MMKV_ID, MMKV.MULTI_PROCESS_MODE)
        } catch (t: Throwable) {
            Log.w(TAG, "mmkvWithID failed, fallback to default", t)
            MMKV.defaultMMKV()
        }

    }


    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        Log.d(TAG, "saveFromResponse: Saving cookies for host: $host")
        val now = System.currentTimeMillis()

        // 打印 incoming cookies 详细信息
        Log.d(TAG, "saveFromResponse: Incoming cookies count=${cookies.size} for host=$host")
        cookies.forEach { Log.d(TAG, "saveFromResponse: incoming: ${formatCookie(it)}") }

        // 原子地合并并替换列表，避免并发修改同一实例
        memoryCache.compute(host) { _, existing ->
            // 基于现有有效 cookie 构建初始列表
            val base = existing?.filter { it.expiresAt > now }?.toMutableList()
                ?: run {
                    val json = mmkv.decodeString(host)
                    if (json != null) {
                        val type = object : TypeToken<List<SerializableCookie>>() {}.type
                        val serializableCookies: List<SerializableCookie> = gson.fromJson(json, type)
                        val loaded = serializableCookies.map { it.toOkHttpCookie() }.filter { it.expiresAt > now }.toMutableList()
                        Log.d(TAG, "saveFromResponse: Loaded ${loaded.size} cookies from MMKV for host: $host")
                        loaded.forEach { Log.d(TAG, "saveFromResponse: loaded from MMKV: ${formatCookie(it)}") }
                        loaded
                    } else {
                        Log.d(TAG, "saveFromResponse: No cookies found in MMKV for host: $host")
                        mutableListOf()
                    }
                }

            // 合并新的 cookies（按 name+domain+path 覆盖）
            cookies.forEach { newCookie ->
                base.removeAll { it.name == newCookie.name && it.domain == newCookie.domain && it.path == newCookie.path }
                if (newCookie.expiresAt > now) {
                    base.add(newCookie)
                }
            }

            // 返回不可变列表作为新的 map 值
            val result = base.filter { it.expiresAt > now }
            Log.d(TAG, "saveFromResponse: After merge cookies count=${result.size} for host=$host")
            result.forEach { Log.d(TAG, "saveFromResponse: merged: ${formatCookie(it)}") }
            result
        }

        jobSave(host)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        Log.d(TAG, "loadForRequest: Loading cookies for host: $host")
        val now = System.currentTimeMillis()

        val list = memoryCache.computeIfAbsent(host) {
            val json = mmkv.decodeString(host)
            Log.d(TAG, "loadForRequest: Reading from MMKV for host: $host, json: $json")
            if (json != null) {
                val type = object : TypeToken<List<SerializableCookie>>() {}.type
                val serializableCookies: List<SerializableCookie> = gson.fromJson(json, type)
                val cookies = serializableCookies.map { it.toOkHttpCookie() }
                Log.d(TAG, "loadForRequest: Loaded ${cookies.size} cookies from MMKV for host: $host")
                cookies.forEach { Log.d(TAG, "loadForRequest: loaded: ${formatCookie(it)}") }
                cookies
            } else {
                Log.d(TAG, "loadForRequest: No cookies found in MMKV for host: $host")
                mutableListOf()
            }
        }

        val validList = list.filter { it.expiresAt > now }
        Log.d(TAG, "loadForRequest: Returning ${validList.size} valid cookies for host: $host")
        validList.forEach { Log.d(TAG, "loadForRequest: returning: ${formatCookie(it)}") }
        return validList
    }

    private fun jobSave(host: String) {
        pendingJobs[host]?.let { job ->
            if (!job.isCompleted && !job.isCancelled) {
                Log.d(TAG, "scheduleSave: cancelling existing job for host=$host")
                job.cancel()
            }
        }
        val job = scope.launch {
            delay(saveDelayMs)
            persistHost(host)
        }
        pendingJobs[host] = job
    }

    private fun persistHost(host: String) {
        val list = memoryCache[host] ?: return
        Log.d(TAG, "persistHost: Persisting ${list.size} cookies for host: $host")
        val now = System.currentTimeMillis()
        val toSave = list.filter { it.expiresAt > now }.map { it ->
            SerializableCookie(
                name = it.name,
                value = it.value,
                expiresAt = it.expiresAt,
                domain = it.domain,
                path = it.path,
                secure = it.secure,
                httpOnly = it.httpOnly,
                hostOnly = it.hostOnly
            )
        }
        Log.d(TAG, "persistHost: Saving ${toSave.size} valid cookies to MMKV for host: $host")
        toSave.forEach {
            val cookie = it.toOkHttpCookie()
            Log.d(TAG, "persistHost: saving: ${formatCookie(cookie)}")
        }
        mmkv.encode(host, gson.toJson(toSave))
        pendingJobs.remove(host)
    }

    fun clear() {
        Log.d(TAG, "clear: Clearing all cookies and cancelling jobs")
        pendingJobs.values.forEach { it.cancel() }
        pendingJobs.clear()
        memoryCache.forEach { (host, list) ->
            Log.d(TAG, "clear: clearing host=$host, cookies=${list.size}")
            list.forEach { Log.d(TAG, "clear: clearing cookie: ${formatCookie(it)}") }
        }
        memoryCache.clear()
        mmkv.clearAll()
        Log.d(TAG, "clear: Cleared MMKV and memory cache")

    }
    fun destroy() {
        scope.cancel()
    }


    private fun formatCookie(cookie: Cookie): String {
        return "name=${cookie.name}, value=${cookie.value}, domain=${cookie.domain}, path=${cookie.path}, expiresAt=${cookie.expiresAt}, secure=${cookie.secure}, httpOnly=${cookie.httpOnly}, hostOnly=${cookie.hostOnly}"
    }
}
