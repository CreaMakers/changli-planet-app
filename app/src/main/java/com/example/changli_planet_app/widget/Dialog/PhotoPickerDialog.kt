package com.example.changli_planet_app.widget.Dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.TextView
import com.example.changli_planet_app.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class PhotoPickerDialog(
    context: Context,
    private val onCameraClick: () -> Unit,
    private val onGalleryClick: () -> Unit
) : BottomSheetDialog(context) {
    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_photo_picker, null)
        setContentView(view)

        behavior.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }

        view.apply {
            findViewById<TextView>(R.id.tv_camera).setOnClickListener {
                dismiss()
                onCameraClick()
            }
            findViewById<TextView>(R.id.tv_gallery).setOnClickListener {
                dismiss()
                onGalleryClick()
            }
            findViewById<TextView>(R.id.tv_dismiss).setOnClickListener {
                dismiss()
            }
        }
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setWindowAnimations(R.style.BottomSheetAnimation)
        }
    }
}