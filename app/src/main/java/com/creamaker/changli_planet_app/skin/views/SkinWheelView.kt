package com.creamaker.changli_planet_app.skin.views

import android.content.Context
import android.util.AttributeSet
import com.creamaker.changli_planet_app.skin.SkinAttributeProvider
import com.creamaker.changli_planet_app.skin.SkinManager
import com.creamaker.changli_planet_app.skin.SkinSupportable
import com.creamaker.changli_planet_app.skin.delegate.SkinDelegate
import com.github.gzuliyujiang.wheelview.widget.WheelView

/**
 * 支持换肤的 WheelView
 * 支持属性:
 * 1. app:wheel_itemTextColor
 * 2. app:wheel_itemTextColorSelected
 * 3. android:background
 */
class SkinWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WheelView(context, attrs, defStyleAttr), SkinSupportable, SkinAttributeProvider {

    private val delegate = SkinDelegate(this, this)

    init {
        delegate.loadSkinAttributes(context, attrs)
        applySkin()
    }

    override fun loadSkinAttributes(context: Context, attrs: AttributeSet?) {
        if (attrs == null) return

        // --- 1. 获取自定义属性 (wheel_itemTextColor) ---
        // 使用 getAttributeResourceValue 避免依赖第三方库的 R.styleable
        // 命名空间 "http://schemas.android.com/apk/res-auto" 对应 XML 中的 app:
        val textColorResId = attrs.getAttributeResourceValue(
            "http://schemas.android.com/apk/res-auto",
            "wheel_itemTextColor",
            0
        )
        if (textColorResId != 0) {
            delegate.setAttr("wheel_text_color", textColorResId)
        }

        // --- 2. 获取自定义属性 (wheel_itemTextColorSelected) ---
        val selectedTextColorResId = attrs.getAttributeResourceValue(
            "http://schemas.android.com/apk/res-auto",
            "wheel_itemTextColorSelected",
            0
        )
        if (selectedTextColorResId != 0) {
            delegate.setAttr("wheel_selected_text_color", selectedTextColorResId)
        }

        // --- 3. 获取通用属性 (android:background) ---
        val bgResId = attrs.getAttributeResourceValue(
            "http://schemas.android.com/apk/res/android",
            "background",
            0
        )
        if (bgResId != 0) {
            delegate.setAttr("view_background", bgResId)
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