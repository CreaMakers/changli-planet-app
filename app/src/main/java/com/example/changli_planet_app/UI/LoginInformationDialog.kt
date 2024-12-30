package com.example.changli_planet_app.UI

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.example.changli_planet_app.R

class LoginInformationDialog(context: Context, val content: String, val type: String) : Dialog(context) {
    lateinit var yes: TextView
    lateinit var contents: TextView
    lateinit var fade : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_dialog)
        contents = findViewById(R.id.content)
        contents.text = content
        yes = findViewById(R.id.yes)
        yes.setOnClickListener {
            dismiss()
        }
        fade = findViewById(R.id.fade)
        fade.text = type
    }
}