package com.example.changli_planet_app.Widget.View

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.appbar.CollapsingToolbarLayout

class NestCollapsingToolbarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CollapsingToolbarLayout(context, attrs) {

    private var mIsScrimsShown: Boolean = false
    private var scrimsShowListener: OnScrimsShowListener? = null

    override fun setScrimsShown(shown: Boolean, animate: Boolean) {
        super.setScrimsShown(shown, animate)
        if (mIsScrimsShown != shown) {
            mIsScrimsShown = shown
            scrimsShowListener?.onScrimsShowChange(this, mIsScrimsShown)
        }
    }

    fun setScrimsShowListener(listener: OnScrimsShowListener) {
        scrimsShowListener = listener
    }

    interface OnScrimsShowListener {
        fun onScrimsShowChange(
            nestCollapsingToolbarLayout: NestCollapsingToolbarLayout,
            isScrimsShow: Boolean
        )
    }
}