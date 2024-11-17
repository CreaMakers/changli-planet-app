package com.example.changli_planet_app.Activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.changli_planet_app.R
import com.example.changli_planet_app.SubjectRepertory
import com.example.changli_planet_app.TimeTableActivityViewModel
import com.example.changli_planet_app.databinding.ActivityAddCourseInTimetableBinding
import com.example.changli_planet_app.databinding.ActivityTimeTableBinding
import com.zhuangfei.timetable.model.Schedule
import kotlinx.coroutines.launch

class AddCourseInTimetable : AppCompatActivity() {
    private val binding by lazy { ActivityAddCourseInTimetableBinding.inflate(layoutInflater) }
    private val timetableBinding by lazy { ActivityTimeTableBinding.inflate(layoutInflater) }

    private val courseName by lazy { binding.customCourseName }
    private val courseRoom by lazy { binding.customCourseRoom }
    private val courseTeacher by lazy { binding.customTeacherName }
    private val courseWeek by lazy { binding.customWeekAndDay }
    private val courseStep by lazy { binding.customCourseStep }
    private val weekDayMap = mapOf(
        1 to "周一",
        2 to "周二",
        3 to "周三",
        4 to "周四",
        5 to "周五",
        6 to "周六",
        7 to "周日"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.hide()
//        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        val timetableViewModel = (application as PlanetApplication).getTimetableViewModel(this)
        val subjects by lazy { TimeTableActivityViewModel.subjects }

        val startCourse = intent.getIntExtra("start", 0)
        courseStep.setText("0$startCourse - 0${startCourse + 1} 节")
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        courseWeek.setText(weekDayMap[intent.getIntExtra("day", 0)])
        binding.customCourseName.addTextChangedListener {
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    lifecycleScope.launch {
                        courseName.setText(it.toString())

                    }

                }

            }

        }
        binding.customCourseRoom.addTextChangedListener {
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    lifecycleScope.launch {
                        courseRoom.setText(it.toString())
                    }
                }

            }


        }
        binding.customTeacherName.addTextChangedListener {
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    lifecycleScope.launch {
                        courseTeacher.setText(it.toString())
                    }

                }

            }


        }





        binding.addCourseBtn.setOnClickListener {
//            timetableBinding.timetableView.apply {
//                source(subjects)
//                updateView()
//
//            }
            val schedule = Schedule()
            if (courseName.text.isNotEmpty()) {
                schedule.name = courseName.text.toString()
            } else {
                Toast.makeText(this, "请输入课程名", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            schedule.day = intent.getIntExtra("day", 0) // 底层的索引从0开始，但计算时却进行了 - 1 ，所以这里要 + 1
            schedule.start = intent.getIntExtra("start", 0)
//        schedule.step = courseStep.text.toString().toInt()

            schedule.step = 2
            if (courseTeacher.text.isNotEmpty()) {
                schedule.teacher = courseTeacher.text.toString()
            } else {
                Toast.makeText(this, "请输入老师名", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (courseRoom.text.isNotEmpty()) {
                schedule.room = courseRoom.text.toString()
            } else {
                Toast.makeText(this, "请输入教室名", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            schedule.weekList =
                listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
            val subject = SubjectRepertory.addSchedule(schedule)
            TimeTableActivityViewModel.addSubject(subject)

            val intent = Intent()
            setResult(RESULT_OK, intent)
            finish()
        }


    }

}