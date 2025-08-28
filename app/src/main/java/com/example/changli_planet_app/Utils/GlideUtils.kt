package com.example.changli_planet_app.Utils

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.load.engine.DiskCacheStrategy
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


fun ImageView.load(imageSource: Any, useDiskCache: Boolean = true) {
    // 如果视图已经具有有效尺寸，直接加载图片
    if (width > 0 && height > 0) {
        Log.d("GlideUtil", "width: $width, height: $height")

        loadImageDirectly(imageSource, useDiskCache, width, height)
        return
    }

    val observer = this.viewTreeObserver
    // 添加全局布局监听器
    observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            /**
             * 检查观察者是否仍然有效 无效的情况
             * 可能是observe的mFloatingTreeObserver merge 到 View 的 mTreeObserver中执行kill()方法
             * 再尝试拿一次width和height
             */
            if (!observer.isAlive) {
                Log.d("GlideUtil", "width: $width, height: $height")
                if (width > 0 && height > 0) {
                    Log.d("GlideUtil", "width: $width, height: $height")
                    loadImageDirectly(imageSource, useDiskCache, width, height)
                }
                return
            }
            Log.d("GlideUtil", "width: $width, height: $height")
            // 获取当前尺寸
            val currentWidth = width
            val currentHeight = height

            // 如果尺寸有效，加载图片并移除监听器
            if (currentWidth > 0 && currentHeight > 0) {
                // 移除监听器
                observer.removeOnGlobalLayoutListener(this)
                // 加载图片
                loadImageDirectly(imageSource, useDiskCache, currentWidth, currentHeight)
            }
        }
    })
}

private fun ImageView.loadImageDirectly(imageSource: Any, useDiskCache: Boolean, pxWidth: Int, pxHeight: Int) {
    GlideApp.with(context)
        .load(imageSource)
        .diskCacheStrategy(if (useDiskCache) DiskCacheStrategy.AUTOMATIC else DiskCacheStrategy.NONE)
        .override(pxWidth, pxHeight) // 按需加载
        .error(R.drawable.ic_error_vector)
        .into(this)
}
