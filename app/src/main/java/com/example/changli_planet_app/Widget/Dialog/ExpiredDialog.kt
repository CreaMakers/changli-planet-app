package com.example.changli_planet_app.Widget.Dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import com.example.changli_planet_app.Base.BaseDialog
import com.example.changli_planet_app.R

class  ExpiredDialog(context: Context, val content: String, val type: String) :
    BaseDialog(context) {
    private lateinit var yes: TextView
    private lateinit var contents: TextView
    private lateinit var fade: TextView

    override fun init() {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setWindowAnimations(R.style.DialogAnimation)
        contents = findViewById(R.id.content)
        contents.text = content
        yes = findViewById(R.id.yes)
        yes.setOnClickListener {
            dismiss()
        }
        fade = findViewById(R.id.fade)
        fade.text = type
    }

    override fun layoutId(): Int =R.layout.login_dialog
}