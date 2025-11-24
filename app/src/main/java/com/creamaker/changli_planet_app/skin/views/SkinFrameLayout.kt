package com.creamaker.changli_planet_app.skin.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import com.creamaker.changli_planet_app.skin.SkinAttributeProvider
import com.creamaker.changli_planet_app.skin.SkinManager
import com.creamaker.changli_planet_app.skin.SkinSupportable
import com.creamaker.changli_planet_app.skin.delegate.SkinDelegate

/**
 * 支持换肤的 FrameLayout
 * 主要支持属性: android:background
 */
class SkinFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), SkinSupportable, SkinAttributeProvider {

    private val delegate = SkinDelegate(this, this)

    init {
        // 初始化时解析属性并应用
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
        // 仅注册观察者，不调用 applySkin()，防止重绘或动画闪烁
        SkinManager.attach(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        SkinManager.detach(this)
    }
}