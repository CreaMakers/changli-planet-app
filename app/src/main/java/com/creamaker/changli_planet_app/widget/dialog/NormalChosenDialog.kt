package com.creamaker.changli_planet_app.widget.dialog

import android.content.Context
import android.graphics.Color
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.base.BaseDialog

open class NormalChosenDialog(
    context: Context,
    private val title: CharSequence,
    private val content: CharSequence,
    private val confirmText: CharSequence = "确定",
    private val cancelText: CharSequence = "取消",
    private val onConfirm: (() -> Unit)? = null,
    private val onCancel: (() -> Unit)? = null
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
        contents.text = content
        yes = findViewById(R.id.chosen_yes)
        yes.text = confirmText
        no = findViewById(R.id.chosen_no)
        no.text = cancelText
        yes.setOnClickListener {
            onConfirm?.invoke()
            dismiss()
        }
        no.setOnClickListener {
            onCancel?.invoke()
            dismiss()
        }
        fade = findViewById(R.id.fade)
        fade.text = title
    }

    override fun layoutId(): Int = R.layout.normal_chosen_dialog
}