package com.example.changli_planet_app.Core

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions

@GlideModule
class PlanetAppGlideModule : AppGlideModule() {
    // 添加内存缓存和 Bitmap 池配置
    val memoryCacheSize = (Runtime.getRuntime().maxMemory() / 4).toLong()
    val bitmapPoolSize = (Runtime.getRuntime().maxMemory() / 6).toLong()

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setMemoryCache(LruResourceCache(memoryCacheSize))
            .setBitmapPool(LruBitmapPool(bitmapPoolSize))
            .setDefaultRequestOptions(
                RequestOptions()
                    .format(DecodeFormat.PREFER_RGB_565)
                    .disallowHardwareConfig()
                    .encodeQuality(85) // JPEG质量
            )
    }

    // 必须重写此方法
    override fun isManifestParsingEnabled() = false
}