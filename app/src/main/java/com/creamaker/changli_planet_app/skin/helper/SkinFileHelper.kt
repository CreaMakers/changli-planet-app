package com.creamaker.changli_planet_app.skin.helper

import android.content.Context
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL

object SkinFileHelper {

    private const val SKIN_CACHE_DIR = "skin_cache"

    suspend fun getSkinFile(
        context: Context,
        name: String
    ): File {

        val cacheFile = getCacheFile(context, name)

        // --- 1. 有缓存直接返回 ---
        if (cacheFile.exists() && cacheFile.length() > 0) {
            return cacheFile
        }

        // --- 2. 尝试从 assets 拷贝 ---
        try {
            context.assets.open(name).use { input ->
                saveStreamToCache(cacheFile, input)
            }
            return cacheFile
        } catch (_: Exception) {
        }
        throw FileNotFoundException(
            "Skin not found in cache, assets, or network: $name"
        )
    }


    /**
     * 写入缓存
     */
    fun saveStreamToCache(
        file: File,
        inputStream: InputStream
    ) {
        file.parentFile?.mkdirs()

        inputStream.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
    }


    /**
     * 获取缓存文件路径
     */
    fun getCacheFile(context: Context, name: String): File {
        val fileName = File(name).name
        return File(File(context.filesDir, SKIN_CACHE_DIR), fileName)
    }
}