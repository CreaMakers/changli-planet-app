package com.example.changli_planet_app.Widget.View

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewParent
import android.widget.ImageView
import com.example.changli_planet_app.R

class AddItemFloatView (context: Context) : BaseFloatView(context)  {
    private val mAdsorbType  = ADSORB_HORIZONTAL
    override fun getChildView(): View {
        return LayoutInflater.from(context).inflate(R.layout.layout_float_button_add_item,null,false)
    }
    override fun getIsCanDrag(): Boolean {
        return true
    }

    override fun getAdsorbType(): Int {
        return mAdsorbType
    }

    override fun getAdsorbTime(): Long {
        return 3000
    }
}