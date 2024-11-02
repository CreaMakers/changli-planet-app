package com.example.changli_planet_app.Activity

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Layout
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import com.example.changli_planet_app.MySubject
import com.example.changli_planet_app.R
import com.example.changli_planet_app.SubjectRepertory
import com.example.changli_planet_app.databinding.ActivityTimeTableBinding
import com.example.changli_planet_app.databinding.CourseinfoDialogBinding
import com.zhuangfei.timetable.TimetableView
import com.zhuangfei.timetable.listener.ISchedule
import com.zhuangfei.timetable.listener.OnFlaglayoutClickAdapter
import com.zhuangfei.timetable.listener.OnItemBuildAdapter
import com.zhuangfei.timetable.listener.OnItemClickAdapter
import com.zhuangfei.timetable.listener.OnSlideBuildAdapter
import com.zhuangfei.timetable.listener.OnSpaceItemClickAdapter
import com.zhuangfei.timetable.model.Schedule


class TimeTable : AppCompatActivity() {
    private val binding by lazy { ActivityTimeTableBinding.inflate(layoutInflater) }
    private val timetableView: TimetableView by lazy { binding.timetableView }
    private val weekView by lazy { binding.weekView }
    private val subjects: MutableList<MySubject> by lazy {
        mutableListOf<MySubject>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        subjects.addAll(SubjectRepertory.loadDefaultSubjects())
        subjects.addAll(SubjectRepertory.loadDefaultSubjects2())
        //加载课表视图
        timetableView.source(subjects)
            .curWeek(9)
            .maxSlideItem(10)
            .curTerm("大二上")
            .cornerAll(15)
            .isShowNotCurWeek(false)


//            .marLeft(5)
            .showView()
        timetableView.apply {
            showTime()     //显示侧边栏时间
            showPopDialog()//课程点击事件,出现弹窗显示课程信息
            buildItemText()//按要求显示课程信息
            addCourse()

        }



        //加载周视图
        weekView.source(subjects)
            .curWeek(9)
            .isShow(true)
            .callback { week -> timetableView.changeWeekOnly(week) }
            .showView()

    }

    private fun showCourseDetailDialog(schedule: Schedule) {
            val dialogBinding = CourseinfoDialogBinding.inflate(layoutInflater)
        dialogBinding.dialogCourseName.text=schedule.name
        dialogBinding.dialogTeacherpart.dialogTeacher.text = schedule.teacher
       dialogBinding.dialogAlarmpart.dialogDayOfWeek.text = schedule.day.toString()
        dialogBinding.dialogPlacepart.dialogPlace.text = schedule.room

        val builder = AlertDialog.Builder(this).apply {
//            setTitle(schedule.name) // 设置课程名称
            setView(dialogBinding.root)
            setPositiveButton("确定", null)

            show()
            Log.d("TimetableView", "showCourseDetailDialog")
        }

    }

    fun TimetableView.showTime() {
        val times = arrayOf(
            "8:00\n8:45", "8:55\n9:40", "10:10\n10:55", "11:05\n11:50",
            "14:00\n14:45", "14:55\n15:40", "16:10\n16:55", "17:05\n17:50",
            "19:30\n20:15", "20:25\n21:10"
        )
        val slideBuildAdapter = OnSlideBuildAdapter()
        slideBuildAdapter.setTimes(times)
            .setTimeTextColor(android.graphics.Color.parseColor("#ADD8E6"))
//            .setTimeTextSize(10F)
        timetableView.callback(slideBuildAdapter)
        timetableView.updateSlideView()
    }

    fun TimetableView.showPopDialog() {

        timetableView.callback(object : OnItemClickAdapter() {
            override fun onItemClick(v: View?, scheduleList: MutableList<Schedule>?) {
                showCourseDetailDialog(v?.tag as Schedule)
            }
        }
        )
    }

    fun TimetableView.buildItemText() {
        timetableView.callback(object : OnItemBuildAdapter() {
            override fun onItemUpdate(
                layout: FrameLayout?,
                textView: TextView?,
                countTextView: TextView?,
                schedule: Schedule?,
                gd: GradientDrawable?
            ) {
                textView?.tag = schedule // 为view 绑定对应的Schedule,方便后续的点击事件
                // 设置 TextView 的内容
                textView?.text = "${schedule?.name}\n\n${schedule?.room}\n\n${schedule?.teacher}"

                // 设置 TextView 的属性
                textView?.apply {
                    textSize = 9f
                    gravity = Gravity.CENTER
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    isSingleLine = false
                    ellipsize = TextUtils.TruncateAt.END
                    setPadding(5, 5, 5, 5)
                }

            }
        })
            .updateView()
    }

    fun TimetableView.addCourse() {
        timetableView.callback(object : OnFlaglayoutClickAdapter() {

            override fun onFlaglayoutClick(day: Int, start: Int) {
                timetableView.hideFlaglayout();
//                                Toast.makeText(context,"55",Toast.LENGTH_SHORT).show()
                val schedule = Schedule()
                schedule.name = "course1"
                schedule.day = day
                schedule.start = start
                schedule.step = 2
                schedule.room = "room1"
                schedule.teacher = "teacher1"
                val subject = SubjectRepertory.addSchedule(schedule)
                subjects.add(subject)
                timetableView.clearData()
            timetableView.source(subjects)
                timetableView.updateView()
                timetableView.buildItemText()
//                Toast.makeText(context,dataSource.size.toString(),Toast.LENGTH_SHORT).show()

            }
        })
    }
    private fun TimetableView.clearData() {
          timetableView.dataSource().clear()
        timetableView.updateView()

    }

}

