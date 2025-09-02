package com.example.changli_planet_app.Feature.ledgar.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.changli_planet_app.Activity.Store.TimeTableStore
import com.example.changli_planet_app.Cache.Room.database.CoursesDataBase
import com.example.changli_planet_app.Cache.Room.entity.MySubject
import com.example.changli_planet_app.Cache.StudentInfoManager
import com.example.changli_planet_app.Core.FullScreenActivity
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.noOpDelegate
import com.example.changli_planet_app.databinding.ActivityAddCourseInTimetableBinding
import com.google.gson.Gson
import kotlinx.coroutines.launch

/**
 * 在课表中添加自定义课程类
 */
class AddCourseActivity : FullScreenActivity() {
    lateinit var coursesDataBase: CoursesDataBase
    lateinit var timeTableStore: TimeTableStore
    private val gson by lazy { Gson() }
    private val binding by lazy { ActivityAddCourseInTimetableBinding.inflate(layoutInflater) }
    private val courseName by lazy { binding.customCourseName }
    private val courseRoom by lazy { binding.customCourseRoom }
    private val courseTeacher by lazy { binding.customTeacherName }
    private val courseWeek by lazy { binding.customWeekAndDay }
    private val courseStep by lazy { binding.customCourseStep }
    private var curWeek: Int = 0
    private val studentId by lazy { StudentInfoManager.studentId }
    private val studentPassword by lazy { StudentInfoManager.studentPassword }
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
        coursesDataBase = CoursesDataBase.getDatabase(PlanetApplication.appContext)
        timeTableStore = TimeTableStore(coursesDataBase.courseDao())
        initView()
        initListener()
    }

    private fun initView() {
        setContentView(binding.root)
        supportActionBar?.hide()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val startCourse = intent.getIntExtra("start", 0)
        curWeek = intent.getIntExtra("curWeek", 0)
        courseStep.setText("0$startCourse - 0${startCourse + 1} 节")
        courseWeek.setText(weekDayMap[intent.getIntExtra("day", 0)])

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar){view,windowInsets->
            val insets=windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                insets.top,
                view.paddingRight,
                view.paddingBottom
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun initListener() {
        binding.customCourseName.addTextChangedListener {
            object : TextWatcher by noOpDelegate() {
                override fun afterTextChanged(s: Editable?) {
                    lifecycleScope.launch {
                        courseName.setText(it.toString())
                    }
                }
            }
        }
        binding.customCourseRoom.addTextChangedListener {
            object : TextWatcher by noOpDelegate() {
                override fun afterTextChanged(s: Editable?) {
                    lifecycleScope.launch {
                        courseRoom.setText(it.toString())
                    }
                }
            }
        }
        binding.customTeacherName.addTextChangedListener {
            object : TextWatcher by noOpDelegate() {
                override fun afterTextChanged(s: Editable?) {
                    lifecycleScope.launch {
                        courseTeacher.setText(it.toString())
                    }
                }
            }
        }

        binding.addCourseBtn.setOnClickListener {
            val mySubject =
                MySubject(isCustom = true, studentId = studentId, studentPassword = studentPassword)
            if (courseName.text.isNotEmpty()) {
                mySubject.courseName = courseName.text.toString()
            } else {
                Toast.makeText(this, "请输入课程名", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            mySubject.apply {
                term = intent.getStringExtra("curTerm")!!
                weekday = intent.getIntExtra("day", 0) // 底层的索引从0开始，但计算时却进行了 - 1 ，所以这里要 + 1
                start = intent.getIntExtra("start", 0)
            }
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
                putExtra("newCourse", gson.toJson(mySubject))
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
}