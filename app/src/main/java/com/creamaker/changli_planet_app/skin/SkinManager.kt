package com.creamaker.changli_planet_app.skin

import android.content.Context
import android.content.res.Resources
import android.util.Log
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.skin.data.cache.SkinCache
import com.creamaker.changli_planet_app.skin.helper.SkinFileHelper
import com.creamaker.changli_planet_app.skin.helper.SkinResourcesHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object SkinManager {

    private const val TAG = "SkinManager"
    private val observers = mutableListOf<java.lang.ref.WeakReference<SkinSupportable>>()
    private  var appContext: Context = PlanetApplication.appContext
   var appResources: Resources = appContext.resources

    // 当前皮肤的资源
    var skinResources: Resources? = null
    var skinPackageName: String? = null
    fun setSkin(name: String) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                if (name.isEmpty()||name == "skin_default") {
                    skinResources = null
                    skinPackageName = null
                    SkinCache.saveAssetsName("skin_default")
                    withContext(Dispatchers.Main) {
                        notifyObservers() // 通知所有 View 重新执行 applySkin
                    }
                    return@launch
                }
                // 1. 获取皮肤文件
                val skinFile = SkinFileHelper.getSkinFile(appContext, name)

                // 2. 加载皮肤资源
                val (resources, pkgName) = SkinResourcesHelper.loadSkinResources(
                    appContext,
                    skinFile
                )

                // 3. 应用皮肤
                skinResources = resources
                skinPackageName = pkgName

                // 保存当前皮肤路径到缓存
                SkinCache.saveAssetsName(name)

                Log.d(TAG, "Skin applied: $name")
                withContext(Dispatchers.Main){
                    notifyObservers()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to set skin: $name", e)
            throw e
        }
    }
    fun attach(view: SkinSupportable) {
        observers.add(java.lang.ref.WeakReference(view))
    }
    fun detach(view: SkinSupportable) {
        val iterator = observers.iterator()
        while (iterator.hasNext()) {
            val ref = iterator.next()
            val observer = ref.get()
            if (observer == null || observer == view) {
                iterator.remove()
            }
        }
    }
    fun notifyObservers() {
        val iterator = observers.iterator()
        while (iterator.hasNext()) {
            val ref = iterator.next()
            val observer = ref.get()
            if (observer != null) {
                // 让 View 重新执行换肤逻辑
                observer.applySkin()
            } else {
                // 顺便清理已经被回收的 View
                iterator.remove()
            }
        }
    }


}