package com.creamaker.changli_planet_app.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.core.PlanetApplication

abstract class BaseDialog(context: Context?) : Dialog(
    context ?: PlanetApplication.appContext, R.style.CustomDialog
) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId())
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        init()
    }

    protected abstract fun init()
    protected abstract fun layoutId(): Int
}