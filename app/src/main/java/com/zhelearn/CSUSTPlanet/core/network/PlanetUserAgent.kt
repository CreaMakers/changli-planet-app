package com.zhelearn.CSUSTPlanet.core.network

import android.os.Build
import com.zhelearn.CSUSTPlanet.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttp

/**
 * Generates the application User-Agent shared by the app's own HTTP clients.
 *
 * Format:
 * CSUSTPlanet/{versionName} ({applicationId}; build:{versionCode}; Android {release}) OkHttp/{version}
 */
object PlanetUserAgent {
    val value: String
        get() = "CSUSTPlanet/${BuildConfig.VERSION_NAME} " +
            "(${BuildConfig.APPLICATION_ID}; build:${BuildConfig.VERSION_CODE}; " +
            "Android ${Build.VERSION.RELEASE}) OkHttp/${OkHttp.VERSION}"

    val interceptor: Interceptor = Interceptor { chain ->
        val request = chain.request()
            .newBuilder()
            .header("User-Agent", value)
            .build()

        chain.proceed(request)
    }
}
