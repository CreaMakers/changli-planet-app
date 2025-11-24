package com.creamaker.changli_planet_app.skin.views

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import com.creamaker.changli_planet_app.skin.SkinAttributeProvider
import com.creamaker.changli_planet_app.skin.SkinManager
import com.creamaker.changli_planet_app.skin.SkinSupportable
import com.creamaker.changli_planet_app.skin.delegate.SkinDelegate
import com.google.android.material.tabs.TabLayout
import com.google.android.material.R as MaterialR

class SkinTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = MaterialR.attr.tabStyle
) : TabLayout(context, attrs, defStyleAttr), SkinSupportable, SkinAttributeProvider {

    private val delegate = SkinDelegate(this, this)

    init {
        delegate.loadSkinAttributes(context, attrs)
        applySkin()
    }

    override fun loadSkinAttributes(context: Context, attrs: AttributeSet?) {
        // 定义我们要抓取的属性数组
        // Index 0: android:background
        // Index 1: app:tabTextColor
        // Index 2: app:tabSelectedTextColor
        context.withStyledAttributes(attrs, intArrayOf(
            android.R.attr.background,
            MaterialR.attr.tabTextColor,
            MaterialR.attr.tabSelectedTextColor
        )) {
            // 1. 背景
            val bgResId = getResourceId(0, 0)
            if (bgResId != 0) {
                delegate.setAttr("view_background", bgResId)
            }

            // 2. 未选中文字颜色
            val textColorResId = getResourceId(1, 0)
            if (textColorResId != 0) {
                delegate.setAttr("tab_text_color", textColorResId)
            }

            // 3. 选中文字颜色
            val selectedColorResId = getResourceId(2, 0)
            if (selectedColorResId != 0) {
                delegate.setAttr("tab_selected_text_color", selectedColorResId)
            }
        }
    }

    override fun applySkin() {
        delegate.applySkin()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        SkinManager.attach(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        SkinManager.detach(this)
    }
}