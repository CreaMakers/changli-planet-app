package com.example.changli_planet_app.feature.mooc.cookie

import okhttp3.Cookie

// 一个可被 Gson 序列化和反序列化的 Cookie 数据类。

data class SerializableCookie(
    val name: String,
    val value: String,
    val expiresAt: Long,
    val domain: String,
    val path: String,
    val secure: Boolean,
    val httpOnly: Boolean,
    val hostOnly: Boolean
) {
//     * 将 SerializableCookie 转换回 okhttp3.Cookie
    fun toOkHttpCookie(): Cookie {
        val builder = Cookie.Builder()
            .name(name)
            .value(value)
            .expiresAt(expiresAt)
            .path(path)

        if (hostOnly) {
            builder.hostOnlyDomain(domain)
        } else {
            builder.domain(domain)
        }

        if (secure) {
            builder.secure()
        }

        if (httpOnly) {
            builder.httpOnly()
        }

        return builder.build()
    }
}
