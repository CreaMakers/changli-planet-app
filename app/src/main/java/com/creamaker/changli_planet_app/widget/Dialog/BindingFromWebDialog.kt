package com.creamaker.changli_planet_app.widget.Dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.lottie.animation.content.Content
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.base.BaseDialog
import com.creamaker.changli_planet_app.core.Route

class BindingFromWebDialog(context: Context, val content: String, val type: String,val webLogin:()->Unit)
    : BaseDialog(context) {
    private lateinit var rebind: TextView
    private lateinit var cancel:TextView
    private lateinit var ps: TextView
    private lateinit var contents: TextView
    private lateinit var fade: TextView
    private lateinit var dismiss:ImageView

    override fun init() {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setWindowAnimations(R.style.DialogAnimation)

        ps = findViewById(R.id.tv_ps)
        ps.visibility = View.GONE

        contents = findViewById(R.id.content)
        contents.text = content

        rebind = findViewById(R.id.rebind)
        rebind.text = "网页绑定"
        rebind.setOnClickListener {
            webLogin()
            dismiss()
        }
        cancel = findViewById(R.id.refresh)
        cancel.text = "暂不绑定"
        cancel.setOnClickListener{
            dismiss()
        }

        dismiss=findViewById(R.id.dismiss)
        dismiss.setOnClickListener{
            dismiss()
        }
        fade = findViewById(R.id.fade)
        fade.text = type
    }

    override fun layoutId(): Int =R.layout.error_stu_password
}