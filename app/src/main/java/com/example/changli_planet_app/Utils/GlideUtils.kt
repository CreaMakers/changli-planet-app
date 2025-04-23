package com.example.changli_planet_app.Utils

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.changli_planet_app.Core.GlideApp
import com.example.changli_planet_app.R

object GlideUtils {
    fun load(
        view: View,
        imageView: ImageView,
        imageSource: Any,
        useDiskCache: Boolean = true
    ) {
        GlideApp.with(view)
            .load(imageSource)
            .diskCacheStrategy(if (useDiskCache) DiskCacheStrategy.AUTOMATIC else DiskCacheStrategy.NONE)
            .error(R.drawable.ic_error_vector)
            .into(imageView)
    }

    fun load(
        fragment: Fragment,
        imageView: ImageView,
        imageSource: Any,
        useDiskCache: Boolean = true
    ) {
        GlideApp.with(fragment)
            .load(imageSource)
            .diskCacheStrategy(if (useDiskCache) DiskCacheStrategy.AUTOMATIC else DiskCacheStrategy.NONE)
            .error(R.drawable.ic_error_vector)
            .into(imageView)
    }

    fun load(
        context: Context,
        imageView: ImageView,
        imageSource: Any,
        useDiskCache: Boolean = true
    ) {
        GlideApp.with(context)
            .load(imageSource)
            .diskCacheStrategy(if (useDiskCache) DiskCacheStrategy.AUTOMATIC else DiskCacheStrategy.NONE)
            .error(R.drawable.ic_error_vector)
            .into(imageView)
    }

    fun loadWithThumbnail(
        view: View,
        imageView: ImageView,
        imageSource: Any,
        useDiskCache: Boolean = true
    ) {
        GlideApp.with(view)
            .load(imageSource)
            .diskCacheStrategy(if (useDiskCache) DiskCacheStrategy.AUTOMATIC else DiskCacheStrategy.NONE)
            .thumbnail(0.5f)
            .error(R.drawable.ic_error_vector)
            .into(imageView)
    }

    fun loadWithThumbnail(
        fragment: Fragment,
        imageView: ImageView,
        imageSource: Any,
        useDiskCache: Boolean = true
    ) {
        GlideApp.with(fragment)
            .load(imageSource)
            .diskCacheStrategy(if (useDiskCache) DiskCacheStrategy.AUTOMATIC else DiskCacheStrategy.NONE)
            .thumbnail(0.5f)
            .error(R.drawable.ic_error_vector)
            .into(imageView)
    }

    fun loadWithThumbnail(
        context: Context,
        imageView: ImageView,
        imageSource: Any,
        useDiskCache: Boolean = true
    ) {
        GlideApp.with(context)
            .load(imageSource)
            .diskCacheStrategy(if (useDiskCache) DiskCacheStrategy.AUTOMATIC else DiskCacheStrategy.NONE)
            .thumbnail(0.5f)
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