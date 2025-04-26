package com.example.changli_planet_app.Widget.Dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.airbnb.lottie.LottieAnimationView
import com.example.changli_planet_app.Base.BaseDialog
import com.example.changli_planet_app.R

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