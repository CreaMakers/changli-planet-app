package com.creamaker.changli_planet_app.core.network

import android.util.Patterns
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

    fun fallbackToLocalDns(hostname: String): List<InetAddress> {
        return try {
            InetAddress.getAllByName(hostname).toList()
        } catch (_: UnknownHostException) {
            emptyList()
        }
    }
}
