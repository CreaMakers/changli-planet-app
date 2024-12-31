package com.example.changli_planet_app.Activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.changli_planet_app.Activity.Store.TimeTableStore
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Cache.Room.CoursesDataBase
import com.example.changli_planet_app.Cache.Room.MySubject
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.ActivityAddCourseInTimetableBinding
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.launch

class AddCourseActivity : AppCompatActivity() {
    lateinit var coursesDataBase: CoursesDataBase
    lateinit var timeTableStore: TimeTableStore
    private val gson by lazy { Gson() }
    private val binding by lazy { ActivityAddCourseInTimetableBinding.inflate(layoutInflater) }
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        coursesDataBase = CoursesDataBase.getDatabase(PlanetApplication.appContext)
        timeTableStore = TimeTableStore(coursesDataBase.courseDao())

        val startCourse = intent.getIntExtra("start", 0)
        val curWeek = intent.getIntExtra("curWeek", 0)
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
            val mySubject = MySubject(isCustom = true)

            if (courseName.text.isNotEmpty()) {
                mySubject.courseName = courseName.text.toString()
            } else {
                Toast.makeText(this, "请输入课程名", Toast.LENGTH_SHORT).show()
//                showSnackbar(binding.root, "请输入课程名")
//                Snackbar.make(binding.root, "请输入课程名", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mySubject.weekday = intent.getIntExtra("day", 0) // 底层的索引从0开始，但计算时却进行了 - 1 ，所以这里要 + 1
            mySubject.start = intent.getIntExtra("start", 0)
//        schedule.step = courseStep.text.toString().toInt()

            mySubject.step = 2
            if (courseTeacher.text.isNotEmpty()) {
                mySubject.teacher = courseTeacher.text.toString()
            } else {
                Toast.makeText(this, "请输入老师名", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (courseRoom.text.isNotEmpty()) {
                mySubject.classroom = courseRoom.text.toString()
            } else {
                Toast.makeText(this, "请输入教室名", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mySubject.weeks = listOf(curWeek)
            val intent = Intent().apply {
//                putExtra("course", mySubject)
                putExtra("newCourse",gson.toJson(mySubject))
            }
            setResult(RESULT_OK, intent)
            finish()
        }
        binding.backBtn.setOnClickListener {
            val intent = Intent()
            setResult(RESULT_OK, intent)
            finish()
        }

    }

    private fun showSnackbar(view: View, text: String) {
        val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT)
        val params = snackbar.view.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin = resources.displayMetrics.heightPixels / 4
        params.width = resources.displayMetrics.widthPixels / 2
        params.leftMargin = resources.displayMetrics.widthPixels / 4
        snackbar.view.layoutParams = params
        snackbar.show()

    }
}