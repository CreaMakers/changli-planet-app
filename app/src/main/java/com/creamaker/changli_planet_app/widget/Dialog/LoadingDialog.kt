package com.creamaker.changli_planet_app.widget.Dialog

import android.content.Context
import android.os.Bundle
import com.airbnb.lottie.LottieAnimationView
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.base.BaseDialog

class LoadingDialog(context: Context) : BaseDialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<LottieAnimationView>(R.id.loadingAnimation).apply {
            background = null
        }
        setContentView(layoutId())
    }

    override fun init() {

    }

    override fun layoutId(): Int = R.layout.dialog_loading
}