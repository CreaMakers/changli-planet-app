package com.creamaker.changli_planet_app.skin.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import com.creamaker.changli_planet_app.skin.SkinAttributeProvider
import com.creamaker.changli_planet_app.skin.SkinManager
import com.creamaker.changli_planet_app.skin.SkinSupportable
import com.creamaker.changli_planet_app.skin.delegate.SkinDelegate
import com.google.android.material.button.MaterialButton
import com.google.android.material.R as MaterialR

class SkinMaterialButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = MaterialR.attr.materialButtonStyle
) : MaterialButton(context, attrs, defStyleAttr), SkinSupportable, SkinAttributeProvider {

    private val delegate = SkinDelegate(this, this)

    init {
        delegate.loadSkinAttributes(context, attrs)
        applySkin()
    }
    @SuppressLint("ResourceType")
    override fun loadSkinAttributes(context: Context, attrs: AttributeSet?) {
        // 1. android:background (通用)
        // 2. android:textColor (通用)
        // 3. app:backgroundTint (MaterialButton 特有)
        // 4. app:strokeColor (描边颜色)
        // 5. app:iconTint (图标颜色)

        context.withStyledAttributes(attrs, intArrayOf(
            android.R.attr.background,
            android.R.attr.textColor,
            MaterialR.attr.backgroundTint,
            MaterialR.attr.strokeColor,
        )) {
            // Index 0: android:background
            val bgResId = getResourceId(0, 0)
            if (bgResId != 0) {
                delegate.setAttr("view_background", bgResId)
            }

            // Index 1: android:textColor
            val textColorResId = getResourceId(1, 0)
            if (textColorResId != 0) {
                delegate.setAttr("tv_text_color", textColorResId)
            }

            // Index 2: app:backgroundTint
            val bgTintResId = getResourceId(2, 0)
            if (bgTintResId != 0) {
                delegate.setAttr("btn_background_tint", bgTintResId)
            }

            // Index 3: app:strokeColor
            val strokeColorResId = getResourceId(3, 0)
            if (strokeColorResId != 0) {
                delegate.setAttr("btn_stroke_color", strokeColorResId)
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