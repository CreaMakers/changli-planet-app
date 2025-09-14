package com.example.changli_planet_app.feature.mooc.cookie

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV
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
    private val memoryCache = mutableMapOf<String, MutableList<Cookie>>()
    private val scheduler = ScheduledThreadPoolExecutor(1).apply {
        //避免被取消的任务长期留在队列里
        removeOnCancelPolicy = true
    }

    //使用线程安全的map
    private val pendingTasks = ConcurrentHashMap<String, ScheduledFuture<*>>()
    private val saveDelayMs = 500L
    private val TAG = "PersistentCookieJar"

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        //从mmkv读取该 host 已有的 cookie
        val host = url.host
        Log.d(TAG, "saveFromResponse: Saving cookies for host: $host")

        //mmkv加载到内存
        val list = memoryCache.getOrPut(host) {
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
        scheduleSave(host)
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        Log.d(TAG, "loadForRequest: Loading cookies for host: $host")
        val now = System.currentTimeMillis()
        val list = memoryCache.getOrPut(host) {
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

    private fun scheduleSave(host: String) {
        pendingTasks[host]?.cancel(false)
        val future = scheduler.schedule({
            persistHost(host)
        }, saveDelayMs, TimeUnit.MILLISECONDS)
        pendingTasks[host] = future
    }

    @Synchronized
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
        pendingTasks.remove(host)
    }

    @Synchronized
    fun clear() {
        Log.d(TAG, "clear: Clearing all cookies")
        // 取消所有计划任务
        pendingTasks.values.forEach { it.cancel(false) }
        pendingTasks.clear()
        Log.d(
            TAG,
            "clear: Cleared pending tasks, memory cache size before clear: ${memoryCache.size}"
        )
        memoryCache.clear()
        mmkv.clearAll()
        Log.d(TAG, "clear: Cleared MMKV and memory cache")
    }
}
