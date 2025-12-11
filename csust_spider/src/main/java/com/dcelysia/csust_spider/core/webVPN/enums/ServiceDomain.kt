package com.dcelysia.csust_spider.core.webVPN.enums

enum class ServiceDomain(
    val scheme: String,
    val directHost: String,
    val vpnHex: String
) {
    AUTH_SERVER(
        scheme = "https",
        directHost = "authserver.csust.edu.cn",
        vpnHex = "57524476706e697374686562657374212a095999a9e22e8d177074d487eb39946bea82e470fa4c"
    ),
    EHALL(
        scheme = "https",
        directHost = "ehall.csust.edu.cn",
        vpnHex = "57524476706e697374686562657374212e144c9db6a93f8807712e9991fa3fceb2f8"
    ),
    MOOC(
        scheme = "http",
        directHost = "pt.csust.edu.cn",
        vpnHex = "57524476706e697374686562657374213b080392a9f22f8f5c673ec2dafd24"
    ),
    EDUCATION(
        scheme = "http",
        directHost = "xk.csust.edu.cn",
        vpnHex = "57524476706e6973746865626573742133170392a9f22f8f5c673ec2dafd24"
    ),
    PHYSICS_EXPERIMENT(
        scheme = "http",
        directHost = "10.255.65.52",
        vpnHex = "57524476706e697374686562657374217a4c03c3efb272cd472c6f85"
    );
}