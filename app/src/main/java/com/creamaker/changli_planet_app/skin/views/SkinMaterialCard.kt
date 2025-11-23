package com.creamaker.changli_planet_app.skin.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log

import androidx.core.content.withStyledAttributes
import com.creamaker.changli_planet_app.skin.SkinAttributeProvider
import com.creamaker.changli_planet_app.skin.SkinManager
import com.creamaker.changli_planet_app.skin.SkinSupportable
import com.creamaker.changli_planet_app.skin.delegate.SkinDelegate
import com.google.android.material.card.MaterialCardView
import androidx.cardview.R
import com.google.android.material.R as materialR
class SkinMaterialCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = materialR.attr.materialCardViewStyle
) : MaterialCardView(context, attrs, defStyleAttr), SkinSupportable, SkinAttributeProvider {

    private val TAG = "SkinMaterialCardView"
    private val delegate = SkinDelegate(this, this)

    init {
        delegate.loadSkinAttributes(context, attrs)
        applySkin()
    }

    @SuppressLint("ResourceType")
    override fun loadSkinAttributes(context: Context, attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, intArrayOf(
            R.attr.cardBackgroundColor
        )) {
            // Index 0: cardBackgroundColor
            val cardBgResId = getResourceId(0, 0)
            if (cardBgResId != 0) {
                delegate.setAttr("card_background_color", cardBgResId)
            }
        }
    }

    override fun applySkin() {
        delegate.applySkin()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(TAG, "onAttachedToWindow")
        SkinManager.attach(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d(TAG, "onDetachedFromWindow")
        SkinManager.detach(this)
    }
}