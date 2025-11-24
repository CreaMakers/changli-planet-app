package com.creamaker.changli_planet_app.skin.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView
import com.creamaker.changli_planet_app.skin.SkinAttributeProvider
import com.creamaker.changli_planet_app.skin.SkinManager
import com.creamaker.changli_planet_app.skin.SkinSupportable
import com.creamaker.changli_planet_app.skin.delegate.SkinDelegate

class SkinImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr), SkinSupportable, SkinAttributeProvider {

    private val TAG = "SkinImageView"
    private val delegate = SkinDelegate(this, this)

    init {
        delegate.loadSkinAttributes(context, attrs)
        applySkin()
    }

    override fun loadSkinAttributes(context: Context, attrs: AttributeSet?) {

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