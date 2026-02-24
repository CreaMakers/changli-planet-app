package com.creamaker.changli_planet_app.feature.mooc.cookie

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import okhttp3.Cookie

// 一个可被 Gson 序列化和反序列化的 Cookie 数据类。

@Keep
data class SerializableCookie(
    @SerializedName("name")
    val name: String?,
    @SerializedName("value")
    val value: String?,
    @SerializedName("expiresAt")
    val expiresAt: Long?,
    @SerializedName("domain")
    val domain: String?,
    @SerializedName("path")
    val path: String?,
    @SerializedName("secure")
    val secure: Boolean,
    @SerializedName("httpOnly")
    val httpOnly: Boolean,
    @SerializedName("hostOnly")
    val hostOnly: Boolean
) {
//     * 将 SerializableCookie 转换回 okhttp3.Cookie
    fun toOkHttpCookieOrNull(): Cookie? {
        val safeName = name?.trim().takeUnless { it.isNullOrEmpty() } ?: return null
        val safeValue = value ?: return null
        val safeDomain = domain?.trim().takeUnless { it.isNullOrEmpty() } ?: return null
        val safePath = path?.trim()?.takeIf { it.startsWith("/") } ?: return null
        val safeExpiresAt = expiresAt?.takeIf { it > 0L } ?: Long.MAX_VALUE

        val builder = Cookie.Builder()
            .name(safeName)
            .value(safeValue)
            .expiresAt(safeExpiresAt)
            .path(safePath)

        if (hostOnly) {
            builder.hostOnlyDomain(safeDomain)
        } else {
            builder.domain(safeDomain)
        }

        if (secure) {
            builder.secure()
        }

        if (httpOnly) {
            builder.httpOnly()
        }

        return runCatching { builder.build() }.getOrNull()
    }
}
