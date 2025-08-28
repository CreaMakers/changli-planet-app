package com.example.changli_planet_app.Utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Path
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.util.TypedValueCompat.pxToDp
import com.example.changli_planet_app.Core.PlanetApplication

object ResourceUtil {
    private val resources:Resources
        get() = PlanetApplication.appContext.resources

    fun getImageSize(ImageView:ImageView): Pair<Int, Int> {
        val lp = ImageView.layoutParams
        var width = if (lp.width>0) lp.width else 0
        var height = if (lp.height>0 ) lp.height else 0
        if (width == 0 && height == 0) {
            ImageView.viewTreeObserver.addOnGlobalLayoutListener {
                width = ImageView.width
                height = ImageView.height
            }
        }
        return width to height
    }

    fun getImageSize(@DrawableRes resId: Int):Pair<Int,Int>{
        val option = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(resources,resId,option)
        return option.outWidth to option.outHeight
    }

    fun getStringRes(@StringRes resId: Int):String{
        return resources.getString(resId)
    }

    fun getColorRes(@ColorRes resId: Int) :Int{
        return ContextCompat.getColor(PlanetApplication.appContext,resId)
    }

    fun getDimen(@DimenRes resId: Int) :Float{
        return resources.getDimension(resId)
    }



}