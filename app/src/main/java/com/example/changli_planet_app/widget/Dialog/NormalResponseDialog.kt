package com.example.changli_planet_app.widget.Dialog

import android.content.Context
import android.graphics.Color
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import com.example.changli_planet_app.R
import com.example.changli_planet_app.base.BaseDialog

class NormalResponseDialog(context: Context, val content: String, val type: String) :
    BaseDialog(context) {
    private lateinit var yes: TextView
    private lateinit var contents: TextView
    private lateinit var fade: TextView

    override fun init() {
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        window?.setWindowAnimations(R.style.DialogAnimation)
        yes = findViewById(R.id.yes)
        yes.setOnClickListener {
            dismiss()
        }
        contents = findViewById(R.id.content)
        contents.text = content
        fade = findViewById(R.id.fade)
        fade.text = type
    }

    override fun layoutId(): Int =R.layout.login_dialog
}