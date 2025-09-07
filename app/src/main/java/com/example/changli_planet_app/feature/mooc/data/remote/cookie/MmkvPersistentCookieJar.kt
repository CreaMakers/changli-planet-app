package com.example.changli_planet_app.feature.mooc.data.remote.cookie

import com.tencent.mmkv.MMKV
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.net.HttpCookie
import java.util.concurrent.ConcurrentHashMap

/**
 * 使用 MMKV 实现的持久化 CookieJar
 * @param context 应用上下文，用于初始化 MMKV
 * @param mmkvId 可选的 MMKV 实例 ID，用于隔离不同用途的 Cookie。默认为 "PersistentCookies"
 */
class MmkvPersistentCookieJar(
    private val context: android.content.Context,
    private val mmkvId: String = "PersistentCookies"
) : CookieJar {

    private val mmkv: MMKV = MMKV.mmkvWithID(mmkvId, MMKV.SINGLE_PROCESS_MODE)
    private val inMemoryCookies = ConcurrentHashMap<String, MutableSet<HttpCookie>>()

    init {
        loadFromMmkv()
    }

    private fun loadFromMmkv() {
        val storedCookies = mmkv.decodeStringSet("cookies") ?: return
        for (cookieStr in storedCookies) {
            val cookies = HttpCookie.parse(cookieStr)
            for (cookie in cookies) {
                val domain = cookie.domain ?: continue
                inMemoryCookies.getOrPut(domain) { mutableSetOf() }.add(cookie)
            }
        }
    }

    private fun saveToMmkv() {
        val allCookies = inMemoryCookies.values.flatten().map { it.toString() }.toSet()
        mmkv.encode("cookies", allCookies)
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return

        for (cookie in cookies) {
            // 过滤掉 session cookie 或明确指定不持久化的 cookie
            if (cookie.persistent) {
                val httpCookie = toHttpCookie(cookie)
                val domain = httpCookie.domain ?: url.host
                inMemoryCookies.getOrPut(domain) { mutableSetOf() }.add(httpCookie)
            }
        }
        saveToMmkv()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val domain = url.host
        val cookies = inMemoryCookies[domain] ?: return emptyList()

        return cookies
            .filter { it.domainMatches(domain) && it.pathMatches(url.encodedPath) }
            .mapNotNull { fromHttpCookie(it, url) }
    }

    /**
     * 清除所有持久化和内存中的 Cookie
     */
    fun clearAllCookies() {
        inMemoryCookies.clear()
        mmkv.removeValueForKey("cookies")
    }

    /**
     * 检查当前是否存储了任何 Cookie
     */
    fun hasAnyCookies(): Boolean {
        return inMemoryCookies.isNotEmpty()
    }

    private fun toHttpCookie(cookie: Cookie): HttpCookie {
        return HttpCookie(cookie.name, cookie.value).apply {
            domain = cookie.domain
            path = cookie.path
            secure = cookie.secure
            // 关键修正: 使用 `expiresAtMillis` 判断，但无法直接设置 `maxAge`
            // 我们在这里不设置 maxAge，因为在 `fromHttpCookie` 中我们无法读取它。
            // 持久化主要依赖 `toString()` 和 `parse()`，它们会处理 Expires/Max-Age 头。
            // 对于持久化，我们更关心它是否是 Session Cookie (通过 expiresAtMillis 判断)
        }
    }

    private fun fromHttpCookie(httpCookie: HttpCookie, url: HttpUrl): Cookie? {
        return try {
            Cookie.Builder()
                .name(httpCookie.name)
                .value(httpCookie.value)
                .domain(httpCookie.domain ?: url.host)
                .path(httpCookie.path ?: "/")
                .apply {
                    if (httpCookie.secure) secure()
                    if (httpCookie.isHttpOnly) httpOnly()
                }
                .build()
        } catch (e: Exception) {
            null
        }
    }

    // 手动实现 domain 匹配
    private fun HttpCookie.domainMatches(requestDomain: String): Boolean {
        // 使用 Android HttpCookie 自带的静态方法
        return HttpCookie.domainMatches(this.domain, requestDomain)
    }

    private fun HttpCookie.pathMatches(requestPath: String): Boolean {
        val cookiePath = this.path ?: "/"
        return requestPath.startsWith(cookiePath)
    }
}