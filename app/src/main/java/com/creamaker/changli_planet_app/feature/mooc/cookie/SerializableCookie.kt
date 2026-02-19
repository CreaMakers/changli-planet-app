package com.creamaker.changli_planet_app.feature.mooc.cookie

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import okhttp3.Cookie

// 一个可被 Gson 序列化和反序列化的 Cookie 数据类。

@Keep
data class SerializableCookie(
    @SerializedName("name")
    val name: String,
    @SerializedName("value")
    val value: String,
    @SerializedName("expiresAt")
    val expiresAt: Long,
    @SerializedName("domain")
    val domain: String,
    @SerializedName("path")
    val path: String,
    @SerializedName("secure")
    val secure: Boolean,
    @SerializedName("httpOnly")
    val httpOnly: Boolean,
    @SerializedName("hostOnly")
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
