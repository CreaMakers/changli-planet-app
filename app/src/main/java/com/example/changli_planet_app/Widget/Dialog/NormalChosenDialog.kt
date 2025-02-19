package com.example.changli_planet_app.Widget.Dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import com.example.changli_planet_app.R

class NormalChosenDialog(
    context: Context,
    val content: String,
    val type: String, private val onConfirm: () -> Unit
) :
    Dialog(context) {
    private lateinit var yes: TextView
    private lateinit var no: TextView
    private lateinit var contents: TextView
    private lateinit var fade: TextView

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setWindowAnimations(R.style.DialogAnimation)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.normal_chosen_dialog)
        contents = findViewById(R.id.content)
        contents.text = content
        yes = findViewById(R.id.chosen_yes)
        no = findViewById(R.id.chosen_no)
        yes.setOnClickListener {
            onConfirm()
            dismiss()
        }
        no.setOnClickListener {
            dismiss()
        }
        fade = findViewById(R.id.fade)
        fade.text = type
    }
}