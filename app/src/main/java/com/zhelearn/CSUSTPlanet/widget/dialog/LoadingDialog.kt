package com.zhelearn.CSUSTPlanet.widget.dialog

import android.content.Context
import android.os.Bundle
import com.airbnb.lottie.LottieAnimationView
import com.zhelearn.CSUSTPlanet.R
import com.zhelearn.CSUSTPlanet.base.BaseDialog

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