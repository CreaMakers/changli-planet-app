package com.example.changli_planet_app.feature.mooc.cookie

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class PersistentCookieJar : CookieJar {
    private val mmkv by lazy { MMKV.defaultMMKV() }
    private val gson = Gson()
    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        // 1. 从mmkv读取该 host 已有的 cookie
        val host = url.host
        val existingJson = mmkv.decodeString(host)
        val existingCookies = if (existingJson != null) {
            val type = object : TypeToken<List<SerializableCookie>>() {}.type
            val serializableCookies: List<SerializableCookie> = gson.fromJson(existingJson, type)
            serializableCookies.map { it.toOkHttpCookie() }.toMutableList()
        } else {
            mutableListOf()
        }

        // 2.合并新旧 cookie，新的覆盖旧的
        cookies.forEach { newCookie ->
            existingCookies.removeAll { it.name == newCookie.name }
            existingCookies.add(newCookie)
        }

        // 3.将合并后的列表转换为 SerializableCookie
        val serializableCookiesToSave = existingCookies.map {
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
        // 4.序列化并存入 MMKV
        mmkv.encode(host, gson.toJson(serializableCookiesToSave))
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        // 1.从 MMKV 读取 JSON 字符串
        val json = mmkv.decodeString(url.host) ?: return emptyList()
        // 2.使用 Gson 和 TypeToken 反序列化为 List<SerializableCookie>
        val type = object : TypeToken<List<SerializableCookie>>() {}.type
        val serializableCookies: List<SerializableCookie> = gson.fromJson(json, type)

        // 3.转换为 okhttp3.Cookie 并过滤掉过期的 cookie
        val currentTime = System.currentTimeMillis()
        return serializableCookies
            .map { it.toOkHttpCookie() }
            .filter { it.expiresAt > currentTime }
    }

    @Synchronized
    fun clear() {
        mmkv.clearAll()
    }
}
