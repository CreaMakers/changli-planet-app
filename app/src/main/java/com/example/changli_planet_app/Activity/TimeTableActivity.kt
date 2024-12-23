package com.example.changli_planet_app.Activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.Activity.Action.TimeTableAction
import com.example.changli_planet_app.Activity.Store.TimeTableStore
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.CoursesDataBase
import com.example.changli_planet_app.Data.jsonbean.GetCourse
import com.example.changli_planet_app.MySubject
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.ActivityTimeTableBinding
import com.example.changli_planet_app.databinding.CourseinfoDialogBinding
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.zhuangfei.timetable.TimetableView
import com.zhuangfei.timetable.listener.OnFlaglayoutClickAdapter
import com.zhuangfei.timetable.listener.OnItemBuildAdapter
import com.zhuangfei.timetable.listener.OnItemClickAdapter
import com.zhuangfei.timetable.listener.OnItemLongClickAdapter
import com.zhuangfei.timetable.listener.OnSlideBuildAdapter
import com.zhuangfei.timetable.model.Schedule
import com.zhuangfei.timetable.view.WeekView
import io.reactivex.rxjava3.disposables.CompositeDisposable


class TimeTableActivity : AppCompatActivity() {
    private val disposables by lazy { CompositeDisposable() }
//    private val timeBinding by lazy { CourseTableTimeBinding.inflate(layoutInflater) }
//
//    private val courseTerm by lazy { timeBinding.courseTerm }
//    private val courseWeek by lazy { timeBinding.courseWeek }
    private val binding by lazy { ActivityTimeTableBinding.inflate(layoutInflater) }
    private val timetableView: TimetableView by lazy { binding.timetableView }
    private val weekView: WeekView by lazy { binding.weekView }
    lateinit var dataBase: CoursesDataBase
    private val timeTableStore: TimeTableStore by lazy {
        TimeTableStore(dataBase.courseDao())
    }
    private val nullTagSubjects by lazy {  mutableListOf(MySubject(term = "null")) }
    private val termList = listOf("2023-2024-1", "2023-2024-2","2024-2025-1","2024-2025-2")
    private val weekList = listOf("第一周","第二周","第三周","第四周","第五周","第六周","第七周","第八周","第九周","第十周","第十一周","第十二周","第十三周","第十四周","第十五周","第十六周","第十七周","第十八周","第十九周","第二十周")
    lateinit var subjects: MutableList<MySubject>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        dataBase = CoursesDataBase.getDatabase(PlanetApplication.appContext)
        if (getValueInMMKV("subjects") != nullTagSubjects) {
            subjects = getValueInMMKV("subjects")
        }
        disposables.add(
            timeTableStore.state().subscribe { curState ->
                subjects = curState.subjects
                timetableView.source(subjects)
                timetableView.updateView()
                weekView.source(subjects)
                weekView.updateView()
            }
        )

        timeTableStore.dispatch(
            TimeTableAction.FetchCourses(
                GetCourse(
                    "202308010135",
                    "@123",
                    "1",
                    "2024-2025-1"
                )
            )
        )


        //加载课表视图
        timetableView
            .curWeek(14)
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
        weekView
            .curWeek(14)
            .isShow(true)
            .callback { week -> timetableView.changeWeekOnly(week) }
            .showView()
//        findViewById<ImageButton>(R.id.courseRefresh).setOnClickListener {
//            timeTableStore.dispatch(
//                TimeTableAction.FetchCourses(
//                    GetCourse(
//                        "202301160231",
//                        "Cy@20050917",
//                        " ",
//                        "2024-2025-1"
//                    )
//                )
//            )
//            timetableView.updateView()
//            weekView.updateView()
//            Toast.makeText(this, "subjects size =${subjects.size}", Toast.LENGTH_SHORT).show()
//        }
//        val termsExtendBtn = timeBinding.termsExtendBtn
//        val weeksExtendBtn = timeBinding.weeksExtendBtn
//        weeksExtendBtn.setOnClickListener {
//            Log.d("TimeTableActivity","weeksExtendBtn is clicked")
//            ClickWheel(weekList)
//            Toast.makeText(this, "week Extend", Toast.LENGTH_SHORT).show()
//        }
//        termsExtendBtn.setOnClickListener {
//            Log.d("TimeTableActivity","termsExtendBtn is clicked")
//            ClickWheel(termList)
//            Toast.makeText(this, "term Extend", Toast.LENGTH_SHORT).show()
//        }
//        weeksExtendBtn.setOnTouchListener { v, event ->
//            Log.d("TimeTableActivity", "weeksExtendBtn touched: $event")
//            false // 返回 false 让事件继续传递
//        }
//
//        termsExtendBtn.setOnTouchListener { v, event ->
//            Log.d("TimeTableActivity", "termsExtendBtn touched: $event")
//            false // 返回 false 让事件继续传递
//        }
//        Log.d("TimeTableActivity", "weeksExtendBtn: $weeksExtendBtn, termsExtendBtn: $termsExtendBtn")
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
        timetableView.hideFlaglayout()
        timetableView.callback(object : OnFlaglayoutClickAdapter() {
            override fun onFlaglayoutClick(day: Int, start: Int) {
                val intent = Intent(context, AddCourseActivity::class.java)
                intent.putExtra("day", day + 1)// 底层的索引从0开始，但计算时却进行了 - 1 ，所以这里要 + 1
                intent.putExtra("start", start)
                intent.putExtra("curWeek",curWeek())
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
        if (::subjects.isInitialized) {
            binding.timetableView.apply {
                source(subjects)
                updateView()
            }
//            Toast.makeText(this, "加载成功 in OnResume", Toast.LENGTH_SHORT).show()
            Toast.makeText(this, "subjects size =${subjects.size}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        storeInMMKV("subjects", subjects)
        disposables.dispose()
    }

    private fun storeInMMKV(key: String, value: List<MySubject>) {
        val mmkv = MMKV.defaultMMKV()
        val json = Gson().toJson(value)
        mmkv.encode(key, json)
    }

    private fun getValueInMMKV(key: String) =
        try {
            Gson().fromJson(MMKV.defaultMMKV().decodeString(key, null), Array<MySubject>::class.java)
                .toMutableList()
        }catch (e : NullPointerException){
            e.printStackTrace()
            nullTagSubjects
        }
//    private fun ClickWheel(item:List<String>){
//
//        val Wheel = TimetableWheelBottomDialog(timeTableStore)
//        Wheel.setItem(item)
//        Wheel.show(supportFragmentManager,"wheel")
//    }
}