package com.dcelysia.cp_common.start_up

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.os.TraceCompat
import com.dcelysia.cp_common.start_up.execption.StartupException
import com.dcelysia.cp_common.start_up.extensions.getUniqueKey
import com.dcelysia.cp_common.start_up.manager.StartupCacheManager
import com.dcelysia.cp_common.start_up.model.StartupProviderStore
import com.dcelysia.cp_common.start_up.provider.StartupProviderConfig

class StartupInitializer {

    companion object {
        @JvmStatic
        val instance by lazy { StartupInitializer() }
        private const val ANDROID_STARTUP = "android.startup"
        private const val ANDROID_STARTUP_PROVIDER_CONFIG = "android.startup.provider.config"
    }

    internal fun discoverAndInitialize(
        context: Context,
        providerName: String
    ): StartupProviderStore {

        TraceCompat.beginSection(StartupInitializer::class.java.simpleName)

        val result = mutableListOf<AndroidStartup<*>>()
        val initialize = mutableListOf<String>()
        val initialized = mutableListOf<String>()
        var config: StartupProviderConfig? = null
        try {
            val provider = ComponentName(context.packageName, providerName)
            val providerInfo =
                context.packageManager.getProviderInfo(provider, PackageManager.GET_META_DATA)
            providerInfo.metaData?.let { metaData ->
                metaData.keySet().forEach { key ->
                    val value = metaData[key]
                    val clazz = Class.forName(key)
                    if (ANDROID_STARTUP == value) {
                        if (AndroidStartup::class.java.isAssignableFrom(clazz)) {
                            doInitialize(
                                (clazz.getDeclaredConstructor().newInstance() as AndroidStartup<*>),
                                result,
                                initialize,
                                initialized
                            )
                        }
                    } else if (ANDROID_STARTUP_PROVIDER_CONFIG == value) {
                        if (StartupProviderConfig::class.java.isAssignableFrom(clazz)) {
                            config = clazz.getDeclaredConstructor()
                                .newInstance() as? StartupProviderConfig
                            // save initialized config
                            StartupCacheManager.instance.saveConfig(config?.getConfig())
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            throw StartupException(t)
        }

        TraceCompat.endSection()

        return StartupProviderStore(result, config)
    }

    private fun doInitialize(
        startup: AndroidStartup<*>,
        result: MutableList<AndroidStartup<*>>,
        initialize: MutableList<String>,
        initialized: MutableList<String>
    ) {
        try {
            val uniqueKey = startup::class.java.getUniqueKey()
            if (initialize.contains(uniqueKey)) {
                throw IllegalStateException("have circle dependencies.")
            }
            if (!initialized.contains(uniqueKey)) {
                initialize.add(uniqueKey)
                result.add(startup)
                if (startup.dependenciesByName().isNullOrEmpty()) {
                    startup.dependencies()?.forEach {
                        doInitialize(
                            it.getDeclaredConstructor().newInstance() as AndroidStartup<*>,
                            result,
                            initialize,
                            initialized
                        )
                    }
                } else {
                    startup.dependenciesByName()?.forEach {
                        val clazz = Class.forName(it)
                        doInitialize(
                            clazz.getDeclaredConstructor().newInstance() as AndroidStartup<*>,
                            result,
                            initialize,
                            initialized
                        )
                    }
                }
                initialize.remove(uniqueKey)
                initialized.add(uniqueKey)
            }
        } catch (t: Throwable) {
            throw StartupException(t)
        }
    }

}