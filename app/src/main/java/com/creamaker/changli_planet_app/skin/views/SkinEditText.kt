package com.creamaker.changli_planet_app.skin.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.creamaker.changli_planet_app.skin.SkinAttributeProvider
import com.creamaker.changli_planet_app.skin.SkinManager
import com.creamaker.changli_planet_app.skin.SkinSupportable
import com.creamaker.changli_planet_app.skin.delegate.SkinDelegate

class SkinEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr), SkinSupportable, SkinAttributeProvider {
    private val defStyle = defStyleAttr
    private val delegate = SkinDelegate(this, this)

    init {
        delegate.loadSkinAttributes(context, attrs)
        applySkin()
    }
    @SuppressLint("ResourceType")
    override fun loadSkinAttributes(context: Context, attrs: AttributeSet?) {
        //这里的创建的数组一定要id从小到大，不然cpp的解析方法会跳过之后的属性，可以跳进attr里查看
        val attrsArray = intArrayOf(
            android.R.attr.textColor,       // Index 0: ID 最小
            android.R.attr.textColorHint,   // Index 1: ID 中间
            android.R.attr.background       // Index 2: ID 最大
        )

        // 强烈建议保留 defStyleAttr，以防万一 XML 没写属性时能读取默认 Style
        val typedArray = context.obtainStyledAttributes(attrs, attrsArray, defStyle, 0)

        try {
            // 1. 文本颜色 (Index 0)
            val textColorResId = typedArray.getResourceId(0, 0)
            if (textColorResId != 0) {
                delegate.setAttr("et_text_color", textColorResId)
            }

            // 2. 提示文本颜色 (Index 1)
            val hintColorResId = typedArray.getResourceId(1, 0)
            if (hintColorResId != 0) {
                delegate.setAttr("et_text_color_hint", hintColorResId)
            }

            // 3. 背景 (Index 2)
            val bgResId = typedArray.getResourceId(2, 0)
            if (bgResId != 0) {
                delegate.setAttr("view_background", bgResId)
            }
        } finally {
            typedArray.recycle()
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