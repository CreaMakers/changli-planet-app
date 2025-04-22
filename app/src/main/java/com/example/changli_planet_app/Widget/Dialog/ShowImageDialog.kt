package com.example.changli_planet_app.Widget.Dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Utils.GlideUtils

class ShowImageDialog(context: Context, private val imageUrl: String) : Dialog(
    context, R.style.CustomDialog
) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId())
        init()
    }

    private fun init() {
        val imageView = findViewById<ImageView>(R.id.preview_image_view)
        GlideUtils.load(
            context,
            imageView,
            imageUrl,
            true
        )
    }

    private fun layoutId() = R.layout.dialog_image_preview
}