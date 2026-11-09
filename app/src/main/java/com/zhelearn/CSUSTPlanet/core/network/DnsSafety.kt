package com.zhelearn.CSUSTPlanet.core.network

import android.util.Patterns
import com.tencent.msdk.dns.MSDKDnsResolver
import okhttp3.Dns
import java.net.InetAddress
import java.net.UnknownHostException

internal object DnsSafety {

    fun sanitizeHostname(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""

        val host = trimmed.substringBefore(';')
            .substringBefore('/')
            .substringBefore('?')
            .substringBefore('#')
            .trim()

        return host
    }

    fun shouldUseHttpDns(hostname: String): Boolean {
        if (hostname.isBlank()) return false
        if (hostname.equals("localhost", ignoreCase = true)) return false
        if (hostname.contains(':')) return false
        if (hostname.contains(" ")) return false
        if (Patterns.IP_ADDRESS.matcher(hostname).matches()) return false
        return true
    }

    fun parseIpList(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return raw.split(';')
            .map { it.trim() }
            .filter { it.isNotBlank() && it != "0" }
            .distinct()
    }

    fun httpDnsResolver(): Dns = object : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            val sanitizedHost = sanitizeHostname(hostname)
            if (sanitizedHost.isBlank()) return emptyList()
            if (!shouldUseHttpDns(sanitizedHost)) return fallbackToLocalDns(sanitizedHost)
            return try {
                val ipArr = parseIpList(MSDKDnsResolver.getInstance().getAddrByName(sanitizedHost))
                if (ipArr.isEmpty()) return fallbackToLocalDns(sanitizedHost)
                val addresses = ipArr.mapNotNull { ip ->
                    runCatching { InetAddress.getByName(ip) }.getOrNull()
                }
                addresses.ifEmpty { fallbackToLocalDns(sanitizedHost) }
            } catch (e: Exception) {
                fallbackToLocalDns(sanitizedHost)
            }
        }
    }

    fun fallbackToLocalDns(hostname: String): List<InetAddress> {
        return try {
            InetAddress.getAllByName(hostname).toList()
        } catch (_: UnknownHostException) {
            emptyList()
        }
    }
}
