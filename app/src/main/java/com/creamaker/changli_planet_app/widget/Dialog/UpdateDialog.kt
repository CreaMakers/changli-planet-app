package com.creamaker.changli_planet_app.widget.Dialog

import android.content.Context
import android.graphics.Color
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.base.BaseDialog
import com.creamaker.changli_planet_app.common.service.DownloadService


class UpdateDialog(
    context: Context,
    private val updateContent: String,
    private val apkUrl: String
) : BaseDialog(context) {

    private lateinit var btnUpdate: TextView
    private lateinit var btnCancel: TextView
    private lateinit var tvUpdateContent: TextView

    override fun init() {//初始化ui
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        window?.setWindowAnimations(R.style.DialogAnimation)
        tvUpdateContent = findViewById(R.id.update_content)

        tvUpdateContent.text = updateContent
        tvUpdateContent.movementMethod = ScrollingMovementMethod()
        btnUpdate = findViewById(R.id.btn_update)
        btnCancel = findViewById(R.id.btn_cancel)

        btnUpdate.setOnClickListener {
            DownloadService().startDownload(context,apkUrl)
            dismiss()
        }
        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun layoutId(): Int = R.layout.update_dialog
}