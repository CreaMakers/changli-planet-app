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
import androidx.core.view.doOnLayout
import com.example.changli_planet_app.core.PlanetApplication

object ResourceUtil {
    private val resources: Resources
        get() = PlanetApplication.appContext.resources

    fun getImageSize(imageView: ImageView): Pair<Int, Int> {
        var currentWidth =  0
        var currentHeight =  0

        imageView.doOnLayout {
            currentHeight = imageView.height
            currentWidth = imageView.width
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