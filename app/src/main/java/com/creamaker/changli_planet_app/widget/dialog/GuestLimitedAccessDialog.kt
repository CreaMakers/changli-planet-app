package com.creamaker.changli_planet_app.widget.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.base.BaseDialog
import com.creamaker.changli_planet_app.core.PlanetApplication
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
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
            NormalChosenDialog(
                context,
                "将清除本地所有缓存哦~",
                "现在进行登录吗",
                onConfirm = {
                    PlanetApplication.Companion.clearCacheAll()
                    Route.goLoginForcibly(context)
                }
            ).show()
            dismiss()
        }
        no.setOnClickListener {
            dismiss()
        }
    }

    override fun layoutId(): Int = R.layout.normal_chosen_dialog
}