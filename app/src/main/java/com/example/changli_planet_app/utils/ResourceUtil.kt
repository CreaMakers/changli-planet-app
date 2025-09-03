package com.example.changli_planet_app.utils

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.example.changli_planet_app.core.PlanetApplication

object ResourceUtil {
    private val resources: Resources
        get() = PlanetApplication.appContext.resources

    fun getImageSize(imageView: ImageView): Pair<Int, Int> {
        val lp = imageView.layoutParams
        var currentWidth = if (lp.width > 0) lp.width else 0
        var currentHeight = if (lp.height > 0) lp.height else 0
        if (currentHeight == 0 && currentWidth == 0) {
            val observer = imageView.viewTreeObserver
            // 添加全局布局监听器
            observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (!observer.isAlive) return
                    Log.d("GlideUtil", "width: ${imageView.width}, height: ${imageView.height}")

                    currentWidth = imageView.width
                    currentHeight = imageView.height

                    if (currentWidth > 0 && currentHeight > 0) {
                        observer.removeOnGlobalLayoutListener(this)
                    }
                }
            })
        }
        return currentWidth to currentHeight
    }

    fun getImageSize(@DrawableRes resId: Int): Pair<Int, Int> {
        val option = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(resources, resId, option)
        return option.outWidth to option.outHeight
    }

    fun getStringRes(@StringRes resId: Int): String {
        return resources.getString(resId)
    }

    fun getColorRes(@ColorRes resId: Int): Int {
        return ContextCompat.getColor(PlanetApplication.appContext, resId)
    }

    fun getDimen(@DimenRes resId: Int): Float {
        return resources.getDimension(resId)
    }


}