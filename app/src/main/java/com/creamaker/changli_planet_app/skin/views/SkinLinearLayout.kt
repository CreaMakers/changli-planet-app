package com.creamaker.changli_planet_app.skin.views


import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import com.creamaker.changli_planet_app.skin.SkinAttributeProvider
import com.creamaker.changli_planet_app.skin.SkinManager
import com.creamaker.changli_planet_app.skin.SkinSupportable
import com.creamaker.changli_planet_app.skin.delegate.SkinDelegate

class SkinLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), SkinSupportable, SkinAttributeProvider {

    private val delegate = SkinDelegate(this, this)

    init {
        // 初始化时加载属性并应用一次
        delegate.loadSkinAttributes(context, attrs)
        applySkin()
    }

    override fun loadSkinAttributes(context: Context, attrs: AttributeSet?) {
        // 只获取 android:background
        context.withStyledAttributes(attrs, intArrayOf(android.R.attr.background)) {
            val bgResId = getResourceId(0, 0)
            if (bgResId != 0) {
                // 复用 delegate 中已有的 "view_background" 逻辑
                delegate.setAttr("view_background", bgResId)
            }
        }
    }

    override fun applySkin() {
        delegate.applySkin()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 仅注册观察者，不再调用 applySkin()
        SkinManager.attach(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        SkinManager.detach(this)
    }
}