package com.example.changli_planet_app.Widget.Dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import com.example.changli_planet_app.R

class LoginInformationDialog(context: Context, val content: String, val type: String) : Dialog(context) {
    lateinit var yes: TextView
    lateinit var contents: TextView
    lateinit var fade : TextView
    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setWindowAnimations(R.style.DialogAnimation)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_dialog)
        contents = findViewById(R.id.content)
        if (content.equals("无效的凭证")) {
            contents.text = "账号或密码错误"
        } else {
            contents.text = content
        }
        yes = findViewById(R.id.yes)
        yes.setOnClickListener {
            dismiss()
        }
        fade = findViewById(R.id.fade)
        fade.text = type
    }
}