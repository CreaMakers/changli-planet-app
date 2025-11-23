package com.creamaker.changli_planet_app.skin.helper

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import android.util.Log
import java.io.File
import java.io.FileNotFoundException

object SkinResourcesHelper {
    /**
     * 加载皮肤包 Resources
     *
     * @param context 应用 context
     * @param skinFile 皮肤包 APK 文件 (已下载或复制到缓存)
     */
    fun loadSkinResources(
        context: Context,
        skinFile: File
    ): Pair<Resources, String /*skinPkgName*/> {

        if (!skinFile.exists()) {
            throw FileNotFoundException("Skin file not found: ${skinFile.path}")
        }

        val skinPath = skinFile.absolutePath

        // 1. 解析皮肤包的 PackageInfo 拿到包名
        val pm = context.packageManager
        val packageInfo = pm.getPackageArchiveInfo(skinPath, 0)
            ?: throw IllegalArgumentException("Invalid skin package: $skinPath")

        val skinPkgName = packageInfo.packageName


        // 2. 创建 AssetManager 并添加 skin apk 的资源路径
        val assetManager = AssetManager::class.java.newInstance()
        val addAssetPathMethod = AssetManager::class.java.getMethod(
            "addAssetPath",
            String::class.java
        )
        addAssetPathMethod.invoke(assetManager, skinPath)


        // 3. 构造 Resources（DisplayMetrics、Configuration 共用系统的）
        val superRes = context.resources
        val skinResources = Resources(
            assetManager,
            superRes.displayMetrics,
            superRes.configuration
        )

        return skinResources to skinPkgName
    }
}