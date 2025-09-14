package com.example.changli_planet_app.feature.mooc.cookie

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gradle.scan.agent.serialization.scan.serializer.kryo.it
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
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.collections.map


class PersistentCookieJar : CookieJar {
    private val mmkv by lazy { MMKV.defaultMMKV() }
    private val gson = Gson()

    //内存缓存
    private val memoryCache = ConcurrentHashMap<String, MutableList<Cookie>>()
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    //使用线程安全的map
    private val pendingJobs = ConcurrentHashMap<String, Job>()
    private val saveDelayMs = 500L
    private val TAG = "PersistentCookieJar"

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        //从mmkv读取该 host 已有的 cookie
        val host = url.host
        Log.d(TAG, "saveFromResponse: Saving cookies for host: $host")

        //mmkv加载到内存
        val list = memoryCache.computeIfAbsent(host) {
            val json = mmkv.decodeString(host)
            Log.d(TAG, "saveFromResponse: Reading from MMKV for host: $host, json: $json")
            if (json != null) {
                val serializableCookies: List<SerializableCookie> = gson
                    .fromJson(json, object : TypeToken<List<SerializableCookie>>() {}.type)
                val cookies = serializableCookies.map { it.toOkHttpCookie() }.toMutableList()
                Log.d(
                    TAG,
                    "saveFromResponse: Loaded ${cookies.size} cookies from MMKV for host: $host"
                )
                cookies
            } else {
                Log.d(TAG, "saveFromResponse: No cookies found in MMKV for host: $host")
                mutableListOf()
            }
        }
        //合并
        cookies.forEach { newCookie ->
            list.removeAll {
                it.name == newCookie.name && it.domain == newCookie.domain && it.path == newCookie.path
            }
            if (newCookie.expiresAt > System.currentTimeMillis())
                list.add(newCookie)
        }
        //过滤过期cookie
        val validList = list.filter { it.expiresAt > System.currentTimeMillis() }.toMutableList()
        memoryCache[host] = validList
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
                val cookies = serializableCookies.map { it.toOkHttpCookie() }.toMutableList()
                Log.d(
                    TAG,
                    "loadForRequest: Loaded ${cookies.size} cookies from MMKV for host: $host"
                )
                cookies
            } else {
                Log.d(TAG, "loadForRequest: No cookies found in MMKV for host: $host")
                mutableListOf()
            }
        }
        //过滤过期cookie
        val validList = list.filter { it.expiresAt > now }
        Log.d(TAG, "loadForRequest: Returning ${validList.size} valid cookies for host: $host")
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
        mmkv.encode(host, gson.toJson(toSave))
        pendingJobs.remove(host)
    }

    fun clear() {
        Log.d(TAG, "clear: Clearing all cookies and cancelling jobs")
        // 取消所有协程任务
        pendingJobs.values.forEach { it.cancel() }
        pendingJobs.clear()
        scope.cancel()
        memoryCache.clear()
        mmkv.clearAll()
        Log.d(TAG, "clear: Cleared MMKV and memory cache")
    }
}
