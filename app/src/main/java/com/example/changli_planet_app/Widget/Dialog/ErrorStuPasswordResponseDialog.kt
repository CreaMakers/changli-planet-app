package com.example.changli_planet_app.Widget.Dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import android.widget.TextView
import com.example.changli_planet_app.Base.BaseDialog
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.R

class  ErrorStuPasswordResponseDialog(context: Context, val content: String, val type: String,val fresh:()->Unit) :
    BaseDialog(context) {
    private lateinit var rebind: TextView
    private lateinit var refresh:TextView
    private lateinit var contents: TextView
    private lateinit var fade: TextView
    private lateinit var dismiss:ImageView

    override fun init() {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setWindowAnimations(R.style.DialogAnimation)

        contents = findViewById(R.id.content)
        contents.text = content
        rebind = findViewById(R.id.rebind)
        rebind.setOnClickListener {
            Route.goBindingUser(context)
            dismiss()
        }
        refresh = findViewById(R.id.refresh)
        refresh.setOnClickListener{
            fresh()
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