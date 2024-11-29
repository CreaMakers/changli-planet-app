package com.example.changli_planet_app.Activity

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.ActivityScoreInquiryBinding
import jp.wasabeef.blurry.Blurry


class ScoreInquiryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScoreInquiryBinding
    protected val backgroundLayout:LinearLayout by lazy { binding.mainInfoLayout }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityScoreInquiryBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        backgroundLayout.post {
            Blurry.with(this)
                .radius(25)
                .sampling(3)
                .color(Color.parseColor("#9950B4FF"))
                .async()
                .onto(backgroundLayout)
        }
    }
}