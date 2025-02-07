package com.example.changli_planet_app.UI

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.changli_planet_app.R

class CustomToast private constructor(context: Context){
    private val toast: Toast
    private val layout: View
    private val textView: TextView

    init {
        toast = Toast(context)
        layout = LayoutInflater.from(context).inflate(R.layout.custom_toast_layout, null)
        textView = layout.findViewById(R.id.toast_text)
        toast.view = layout
        toast.duration = Toast.LENGTH_SHORT
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: CustomToast? = null

        fun showMessage(context: Context, message: String) {
            if(instance == null) {
                instance = CustomToast(context.applicationContext)
            }
            instance?.show(message)
        }
    }

    fun show(message: String) {
        textView.text = message
        toast.show()
    }
}