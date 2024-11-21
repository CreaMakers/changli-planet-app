package com.example.changli_planet_app.Activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextUtils
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
import com.example.changli_planet_app.MySubject
import com.example.changli_planet_app.R
import com.example.changli_planet_app.TimeTableActivityViewModel
import com.example.changli_planet_app.databinding.ActivityTimeTableBinding
import com.example.changli_planet_app.databinding.CourseinfoDialogBinding
import com.zhuangfei.timetable.TimetableView
import com.zhuangfei.timetable.listener.OnFlaglayoutClickAdapter
import com.zhuangfei.timetable.listener.OnItemBuildAdapter
import com.zhuangfei.timetable.listener.OnItemClickAdapter
import com.zhuangfei.timetable.listener.OnItemLongClickAdapter
import com.zhuangfei.timetable.listener.OnSlideBuildAdapter
import com.zhuangfei.timetable.model.Schedule
import com.zhuangfei.timetable.view.WeekView


class TimeTableActivity : AppCompatActivity() {

    //lateinit var viewModel: TimeTableActivityViewModel
    private val binding by lazy { ActivityTimeTableBinding.inflate(layoutInflater) }
    private val timetableView: TimetableView by lazy { binding.timetableView }
    private val weekView: WeekView by lazy { binding.weekView }
    val subjects: MutableList<MySubject> by lazy {
        TimeTableActivityViewModel.subjects
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
//         viewModel = (application as PlanetApplication).getTimetableViewModel(this)

        //加载课表视图
        timetableView.source(subjects)
            .curWeek(9)
            .maxSlideItem(10)
            .curTerm("大二上")
            .cornerAll(15)
            .isShowNotCurWeek(false)

            .showView()
        timetableView.apply {
            showTime()     //显示侧边栏时间
            showPopDialog()//课程点击事件,出现弹窗显示课程信息
            buildItemText()//按要求显示课程信息
            addCourse()    //添加自定义课程
            longClickToDeleteCourse()
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
        dialogBinding.dialogCourseName.text = schedule.name
        dialogBinding.dialogPlacepart.dialogPlace.text = schedule.room
        dialogBinding.dialogTeacherpart.dialogTeacher.text = schedule.teacher
        dialogBinding.dialogWeekpart.dialogWeek.text =
            "${schedule.weekList[0]} - ${schedule.weekList.last()} (周)"
        dialogBinding.dialogAlarmpart.dialogCourseStep.text =
            "${schedule.start} - ${(schedule.start - 1 + schedule.step)} 节"


        AlertDialog.Builder(this).apply {
            setView(dialogBinding.root)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
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
                val intent = Intent(context, AddCourseActivity::class.java)
                intent.putExtra("day", day + 1)// 底层的索引从0开始，但计算时却进行了 - 1 ，所以这里要 + 1
                intent.putExtra("start", start)
                startActivity(intent)
            }
        })

    }

    fun TimetableView.longClickToDeleteCourse() {
        timetableView.callback(object : OnItemLongClickAdapter() {
            override fun onLongClick(v: View?, day: Int, start: Int) {

            }
        })
    }


    override fun onResume() {
        super.onResume()
        binding.timetableView.apply {
            source(TimeTableActivityViewModel.subjects)
            updateView()

        }
        Toast.makeText(this, "subjects size =${subjects.size}", Toast.LENGTH_SHORT).show()
    }
}


