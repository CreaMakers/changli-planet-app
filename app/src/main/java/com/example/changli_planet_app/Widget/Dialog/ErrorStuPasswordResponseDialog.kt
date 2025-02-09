package com.example.changli_planet_app.Widget.Dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.R

class  ErrorStuPasswordResponseDialog(context: Context, val content: String, val type: String) :
    Dialog(context) {
    private lateinit var yes: TextView
    private lateinit var contents: TextView
    private lateinit var fade: TextView
    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setWindowAnimations(R.style.DialogAnimation)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_dialog)
        contents = findViewById(R.id.content)
        contents.text = content
        yes = findViewById(R.id.yes)
        yes.setOnClickListener {
            Route.goBindingUser(context)
            dismiss()
        }
        fade = findViewById(R.id.fade)
        fade.text = type
    }
}