package com.creamaker.changli_planet_app.skin.views

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import com.creamaker.changli_planet_app.skin.SkinAttributeProvider
import com.creamaker.changli_planet_app.skin.SkinManager
import com.creamaker.changli_planet_app.skin.SkinSupportable
import com.creamaker.changli_planet_app.skin.delegate.SkinDelegate

/**
 * 支持换肤的 ConstraintLayout
 * 主要支持属性: android:background
 */
class SkinConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), SkinSupportable, SkinAttributeProvider {

    private val TAG = "SkinConstraintLayout"
    // 复用通用的 SkinDelegate
    private val delegate = SkinDelegate(this, this)

    init {
        delegate.loadSkinAttributes(context, attrs)
        applySkin()
    }

    override fun loadSkinAttributes(context: Context, attrs: AttributeSet?) {
        // 只需要获取 android:background 属性
        context.withStyledAttributes(attrs, intArrayOf(android.R.attr.background)) {
            val bgResId = getResourceId(0, 0)
            if (bgResId != 0) {
                // 使用通用的 key "view_background"，请确保 SkinDelegate 中已处理此 key
                delegate.setAttr("view_background", bgResId)
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