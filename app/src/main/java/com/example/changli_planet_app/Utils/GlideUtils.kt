package com.example.changli_planet_app.Utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestListener
import com.example.changli_planet_app.Core.GlideApp
import com.example.changli_planet_app.R

object GlideUtils {
    private val TAG = "GlideUtils"
    fun load(
        view: View,
        imageView: ImageView,
        imageSource: Any,
        useDiskCache: Boolean = true
    ) {
        val heightAndwidth = ResourceUtil.getImageSize(imageView)
        GlideApp.with(view)
            .load(imageSource)
            .diskCacheStrategy(if (useDiskCache) DiskCacheStrategy.AUTOMATIC else DiskCacheStrategy.NONE)
            .error(R.drawable.ic_error_vector)
            .override(heightAndwidth.first,heightAndwidth.second)
            .into(imageView)
    }

    fun load(
        fragment: Fragment,
        imageView: ImageView,
        imageSource: Any,
        useDiskCache: Boolean = true
    ) {
        val heightAndwidth = ResourceUtil.getImageSize(imageView)
        GlideApp.with(fragment)
            .load(imageSource)
            .diskCacheStrategy(if (useDiskCache) DiskCacheStrategy.AUTOMATIC else DiskCacheStrategy.NONE)
            .override(heightAndwidth.first,heightAndwidth.second)
            .error(R.drawable.ic_error_vector)
            .into(imageView)
    }

    fun load(
        context: Context,
        imageView: ImageView,
        imageSource: Any,
        useDiskCache: Boolean = true
    ) {
        val heightAndwidth = ResourceUtil.getImageSize(imageView)
        GlideApp.with(context)
            .load(imageSource)
            .diskCacheStrategy(if (useDiskCache) DiskCacheStrategy.AUTOMATIC else DiskCacheStrategy.NONE)
            .error(R.drawable.ic_error_vector)
            .override(heightAndwidth.first,heightAndwidth.second)
            .into(imageView)
    }

    fun loadWithThumbnail(
        view: View,
        imageView: ImageView,
        imageSource: Any,
        useDiskCache: Boolean = true
    ) {
        val heightAndwidth = ResourceUtil.getImageSize(imageView)
        GlideApp.with(view)
            .load(imageSource)
            .diskCacheStrategy(if (useDiskCache) DiskCacheStrategy.AUTOMATIC else DiskCacheStrategy.NONE)
            .thumbnail(0.5f)
            .error(R.drawable.ic_error_vector)
            .override(heightAndwidth.first,heightAndwidth.second)
            .into(imageView)
    }

    fun loadWithThumbnail(
        fragment: Fragment,
        imageView: ImageView,
        imageSource: Any,
        useDiskCache: Boolean = true
    ) {
        val heightAndwidth = ResourceUtil.getImageSize(imageView)
        GlideApp.with(fragment)
            .load(imageSource)
            .diskCacheStrategy(if (useDiskCache) DiskCacheStrategy.AUTOMATIC else DiskCacheStrategy.NONE)
            .thumbnail(0.5f)
            .override(heightAndwidth.first,heightAndwidth.second)
            .error(R.drawable.ic_error_vector)
            .into(imageView)
    }

    fun loadWithThumbnail(
        context: Context,
        imageView: ImageView,
        imageSource: Any,
        useDiskCache: Boolean = true
    ) {
        val heightAndwidth = ResourceUtil.getImageSize(imageView)
        GlideApp.with(context)
            .load(imageSource)
            .diskCacheStrategy(if (useDiskCache) DiskCacheStrategy.AUTOMATIC else DiskCacheStrategy.NONE)
            .thumbnail(0.5f)
            .override(heightAndwidth.first,heightAndwidth.second)
            .error(R.drawable.ic_error_vector)
            .into(imageView)
    }

    // 预加载图片
    fun preload(context: Context, imageUrls: List<String>, useDiskCache: Boolean = true) {
        imageUrls.forEach { url ->
            GlideApp.with(context)
                .load(url)
                .diskCacheStrategy(if (useDiskCache) DiskCacheStrategy.AUTOMATIC else DiskCacheStrategy.NONE)
                .preload()
        }
    }
}

/**
 * 加载图片资源
 *
 * 注意：SVG矢量图建议使用原生方式加载以获得更好性能：
 * imageView.setImageResource(R.drawable.ic_svg_image)
 *
 * @param imageSource 加载图片资源，支持多种类型：
 *                   - Int: 资源ID
 *                   - String: 图片URL或文件路径
 *                   - Uri: 图片URI
 *                   - File: 图片文件
 *                   - Drawable: Drawable对象
 *                   - Bitmap: Bitmap对象
 * @param pxWidth imageView宽度，单位px
 * @param pxHeight imageView高度，单位px
 * @param useDiskCache 是否启用磁盘缓存
 * @param useMemoryCache 是否启用内存缓存
 * @param listener glide RequestListener回调
 */
fun ImageView.load(
    imageSource: Any,
    pxWidth: Int = 0,
    pxHeight: Int = 0,
    useDiskCache: Boolean = true,
    useMemoryCache: Boolean = true,
    listener: RequestListener<Drawable>? = null
) {
    Log.d("dcelysia", "ImageView.load start: $imageSource")
    if (pxWidth != 0 || pxHeight != 0) {
        Log.d("dcelysia", "ImageView.load 用户设置 pxWidth: $pxWidth, pxHeight: $pxHeight")
        loadImageDirectly(imageSource, useDiskCache, useMemoryCache, pxWidth, pxHeight, listener)
    } else {
        doOnLayout {
            Log.d("dcelysia", "ImageView.load already width: $width, height: $height")
            loadImageDirectly(imageSource, useDiskCache, useMemoryCache, width, height, listener)
        }
    }
}

private fun ImageView.loadImageDirectly(
    imageSource: Any,
    useDiskCache: Boolean,
    useMemoryCache: Boolean,
    pxWidth: Int,
    pxHeight: Int,
    listener: RequestListener<Drawable>? = null
) {
    Log.d("dcelysia", "ImageView loadImageDirectly: $imageSource")
    GlideApp.with(context)
        .load(imageSource)
        .skipMemoryCache(!useMemoryCache)
        .diskCacheStrategy(if (useDiskCache) DiskCacheStrategy.AUTOMATIC else DiskCacheStrategy.NONE)
        .listener(listener)
        .override(pxWidth, pxHeight) // 按需加载
        .error(R.drawable.ic_error_vector)
        .into(this)
}
