package com.example.changli_planet_app.Activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Widget.Picker.LessonPicker
import com.example.changli_planet_app.databinding.ActivityClassInfoBinding

class ClassInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClassInfoBinding
    private val query by lazy { binding.classQueryButton }
    private val back by lazy { binding.personProfileBack }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityClassInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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