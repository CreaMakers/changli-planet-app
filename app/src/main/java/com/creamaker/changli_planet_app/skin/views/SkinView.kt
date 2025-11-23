package com.creamaker.changli_planet_app.skin.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.creamaker.changli_planet_app.skin.SkinAttributeProvider
import com.creamaker.changli_planet_app.skin.SkinManager
import com.creamaker.changli_planet_app.skin.SkinSupportable
import com.creamaker.changli_planet_app.skin.delegate.SkinDelegate

/**
 * 支持换肤的基础 View
 * 适用于仅作为背景容器、分隔线等用途的 <View />
 */
class SkinView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), SkinSupportable, SkinAttributeProvider {

    private val delegate = SkinDelegate(this, this)

    init {
        delegate.loadSkinAttributes(context, attrs)
        applySkin()
    }

    override fun loadSkinAttributes(context: Context, attrs: AttributeSet?) {
        // 只获取 android:background 属性
        context.withStyledAttributes(attrs, intArrayOf(android.R.attr.background)) {
            val bgResId = getResourceId(0, 0)
            if (bgResId != 0) {
                // 使用通用的 key "view_background"
                delegate.setAttr("view_background", bgResId)
            }
        }
    }

    override fun applySkin() {
        delegate.applySkin()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 仅注册，不立即重绘，避免 Ripple 或动画重置问题
        SkinManager.attach(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        SkinManager.detach(this)
    }
}