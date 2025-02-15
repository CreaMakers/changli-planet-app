package com.example.changli_planet_app.Activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.Core.FullScreenActivity
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Widget.Picker.LessonPicker
import com.example.changli_planet_app.databinding.ActivityClassInfoBinding

class ClassInfoActivity : FullScreenActivity() {
    private lateinit var binding: ActivityClassInfoBinding
    private val query by lazy { binding.classQueryButton }
    private val back by lazy { binding.personProfileBack }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListener()
    }

    private fun initView(){
        binding = ActivityClassInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun initListener(){
        back.setOnClickListener { finish() }
        query.setOnClickListener {
            val lessonPicker = LessonPicker(this)
            lessonPicker.setOnLessonSelectedListener { start, end ->
                Toast.makeText(this, "从第${start}节到第${end}节", Toast.LENGTH_SHORT).show()
            }
            lessonPicker.show()
        }
    }
}