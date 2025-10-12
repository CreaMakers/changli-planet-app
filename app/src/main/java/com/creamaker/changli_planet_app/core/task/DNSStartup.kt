package com.creamaker.changli_planet_app.core.task

import android.content.Context
import android.util.Log
import com.dcelysia.cp_common.start_up.AndroidStartup
import com.dcelysia.cp_common.start_up.Startup
import com.tencent.msdk.dns.DnsConfig
import com.tencent.msdk.dns.MSDKDnsResolver

class DNSStartup : AndroidStartup<Unit>() {

    override fun create(context: Context): Unit {
        try {
            val dnsConfigBuilder = DnsConfig.Builder()
                .dnsId("98468")
                .token("884069233")
                .https()
                .logLevel(Log.VERBOSE)
                .build()
            MSDKDnsResolver.getInstance().init(context, dnsConfigBuilder)
        } catch (e: Exception) {
            Log.e("DNSStartup", "DNS initialization failed", e)
        }
    }

    override fun dependencies(): List<Class<out Startup<*>>>? = null

    override fun callCreateOnMainThread(): Boolean = false

    override fun waitOnMainThread(): Boolean = false
}