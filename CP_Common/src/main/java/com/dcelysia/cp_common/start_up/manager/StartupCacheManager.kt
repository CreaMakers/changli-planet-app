package com.dcelysia.cp_common.start_up.manager

import com.dcelysia.cp_common.start_up.Startup
import com.dcelysia.cp_common.start_up.model.ResultModel
import com.dcelysia.cp_common.start_up.model.StartupConfig
import java.util.concurrent.ConcurrentHashMap

class StartupCacheManager {

    /**
     * Save initialized components result.
     */
    private val mInitializedComponents = ConcurrentHashMap<Class<out Startup<*>>, ResultModel<*>>()
    var initializedConfig: StartupConfig? = null
        private set

    companion object {
        @JvmStatic
        val instance by lazy { StartupCacheManager() }
    }

    /**
     * save result of initialized component.
     */
    internal fun saveInitializedComponent(zClass: Class<out Startup<*>>, result: ResultModel<*>) {
        mInitializedComponents[zClass] = result
    }

    /**
     * check initialized.
     */
    fun hadInitialized(zClass: Class<out Startup<*>>): Boolean =
        mInitializedComponents.containsKey(zClass)

    @Suppress("UNCHECKED_CAST")
    fun <T> obtainInitializedResult(zClass: Class<out Startup<*>>): T? =
        mInitializedComponents[zClass]?.result as? T?

    fun remove(zClass: Class<out Startup<*>>) {
        mInitializedComponents.remove(zClass)
    }

    fun clear() {
        mInitializedComponents.clear()
    }

    /**
     * save initialized config.
     */
    internal fun saveConfig(config: StartupConfig?) {
        initializedConfig = config
    }
}