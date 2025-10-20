package com.creamaker.changli_planet_app.widget.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.creamaker.changli_planet_app.R

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