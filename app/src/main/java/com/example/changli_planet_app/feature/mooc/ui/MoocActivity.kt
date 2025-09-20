package com.example.changli_planet_app.feature.mooc.ui

import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.cardview.widget.CardView
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import com.example.changli_planet_app.R
import com.example.changli_planet_app.common.data.local.mmkv.StudentInfoManager.studentId
import com.example.changli_planet_app.common.data.local.mmkv.StudentInfoManager.studentPassword
import com.example.changli_planet_app.core.Route
import com.example.changli_planet_app.widget.View.CustomToast.Companion.showMessage

class MoocActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = Color.White) {
                MoocScreen()
            }
        }
        if (studentId.isEmpty() || studentPassword.isEmpty()) {
            showMessage(getString(R.string.bind_notification))
            Route.goBindingUser(this)
            finish()
            return  // 这里直接返回了，后续代码不会执行
        }


    }
    private fun showMessage(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).apply {
            val cardView = CardView(applicationContext).apply {
                radius = 25f
                cardElevation = 8f
                setCardBackgroundColor(getColor(R.color.score_bar))
                useCompatPadding = true
            }

            val textView = TextView(applicationContext).apply {
                text = message
                textSize = 17f
                setTextColor(android.graphics.Color.BLACK)
                gravity = Gravity.CENTER
                setPadding(80, 40, 80, 40)
            }
            cardView.addView(textView)
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 140)
            view = cardView
            show()
        }
    }
}