package com.example.changli_planet_app.Core

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Widget.Dialog.LoadingDialog

open class FullScreenActivity : AppCompatActivity() {
    private val loadingDialog by lazy { LoadingDialog(this@FullScreenActivity) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun showCatLoading() {
        loadingDialog.show()
    }

    fun dismissCatLoading() {
        loadingDialog.dismiss()
    }
}