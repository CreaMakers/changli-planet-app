package com.creamaker.changli_planet_app.widget.dialog

import android.content.Context
import android.graphics.Color
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.base.BaseDialog
import com.creamaker.changli_planet_app.core.Route

class GuestLimitedAccessDialog(
    context: Context
) :
    BaseDialog(context) {
    private lateinit var yes: TextView
    private lateinit var no: TextView
    private lateinit var contents: TextView
    private lateinit var fade: TextView

    override fun init() {
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        window?.setWindowAnimations(R.style.DialogAnimation)

        contents = findViewById(R.id.content)
        contents.text = "当前功能需要登录后才能使用，请先登录！"
        fade = findViewById(R.id.fade)
        fade.text = "进入未知区域了哦~"

        yes = findViewById(R.id.chosen_yes)
        yes.text = "现在登录"
        no = findViewById(R.id.chosen_no)
        no.text = "我再看看"

        yes.setOnClickListener {
            Route.goLoginForcibly(context)
            dismiss()
        }
        no.setOnClickListener {
            dismiss()
        }
    }

    override fun layoutId(): Int = R.layout.normal_chosen_dialog
}