package com.dcelysia.csust_spider.core.webVPN

import com.dcelysia.csust_spider.core.webVPN.enums.ConnectionMode
import com.dcelysia.csust_spider.core.webVPN.enums.ServiceDomain

class URLFactory(val mode: ConnectionMode) {
    private val vpnBase = "https://vpn.csust.edu.cn"
    fun make(
        domain: ServiceDomain,
        path: String,
        mode: ConnectionMode = this.mode
    ): String {
        val safePath = if (path.startsWith("/")) path else "/$path"

        return when (mode) {
            ConnectionMode.direct -> {
                "${domain.scheme}://${domain.directHost}$safePath"
            }

            ConnectionMode.web_vpn -> {
                "$vpnBase/${domain.scheme}/${domain.vpnHex}$safePath"
            }
        }
    }
}