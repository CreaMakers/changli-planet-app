package com.zhelearn.CSUSTPlanet.skin.views

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import com.zhelearn.CSUSTPlanet.skin.SkinAttributeProvider
import com.zhelearn.CSUSTPlanet.skin.SkinManager
import com.zhelearn.CSUSTPlanet.skin.SkinSupportable
import com.zhelearn.CSUSTPlanet.skin.delegate.SkinDelegate
import com.zhelearn.CSUSTPlanet.widget.view.MaxHeightLinearLayout // 引入你的自定义父类

class SkinMaxHeightLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaxHeightLinearLayout(context, attrs, defStyleAttr), SkinSupportable, SkinAttributeProvider {

    private val delegate = SkinDelegate(this, this)

    init {
        // 初始化时解析属性并应用
        delegate.loadSkinAttributes(context, attrs)
        applySkin()
    }

    override fun loadSkinAttributes(context: Context, attrs: AttributeSet?) {
        // 只获取 android:background 属性
        // 如果 MaxHeightLinearLayout 有自定义属性(比如边框颜色)需要换肤，也可以在这里添加
        context.withStyledAttributes(attrs, intArrayOf(android.R.attr.background)) {
            val bgResId = getResourceId(0, 0)
            if (bgResId != 0) {
                // 复用通用的 "view_background" key
                delegate.setAttr("view_background", bgResId)
            }
        }
    }

    override fun applySkin() {
        delegate.applySkin()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 仅注册观察者，不调用 applySkin()
        SkinManager.attach(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        SkinManager.detach(this)
    }
}