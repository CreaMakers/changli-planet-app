package com.example.changli_planet_app.Activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
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
import com.example.changli_planet_app.databinding.CourseTableTimeBinding
import com.example.changli_planet_app.databinding.CourseinfoDialogBinding
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.zhuangfei.timetable.TimetableView
import com.zhuangfei.timetable.listener.ISchedule
import com.zhuangfei.timetable.listener.IWeekView
import com.zhuangfei.timetable.listener.OnFlaglayoutClickAdapter
import com.zhuangfei.timetable.listener.OnItemBuildAdapter
import com.zhuangfei.timetable.listener.OnItemClickAdapter
import com.zhuangfei.timetable.listener.OnItemLongClickAdapter
import com.zhuangfei.timetable.listener.OnSlideBuildAdapter
import com.zhuangfei.timetable.model.Schedule
import com.zhuangfei.timetable.view.WeekView
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class TimeTableActivity : AppCompatActivity() {
    private val disposables by lazy { CompositeDisposable() }

    private val timeBinding by lazy { CourseTableTimeBinding.inflate(layoutInflater) }

    //
    private val courseTerm by lazy { timeBinding.courseTerm }
    private val courseWeek by lazy { timeBinding.courseWeek }
    private var curDisplayWeek = 0
    private val binding by lazy { ActivityTimeTableBinding.inflate(layoutInflater) }
    private val timetableView: TimetableView by lazy { binding.timetableView }
    private val weekView: WeekView by lazy { binding.weekView }
    lateinit var dataBase: CoursesDataBase
    private val timeTableStore: TimeTableStore by lazy {
        TimeTableStore(dataBase.courseDao())
    }
    private val nullTagSubjects by lazy { mutableListOf(MySubject(term = "null")) }
    private val termList = listOf("2023-2024-1", "2023-2024-2", "2024-2025-1", "2024-2025-2")
    private val weekList = listOf(
        "第一周",
        "第二周",
        "第三周",
        "第四周",
        "第五周",
        "第六周",
        "第七周",
        "第八周",
        "第九周",
        "第十周",
        "第十一周",
        "第十二周",
        "第十三周",
        "第十四周",
        "第十五周",
        "第十六周",
        "第十七周",
        "第十八周",
        "第十九周",
        "第二十周"
    )
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


        // 初始化 TimetableView
        timetableView
            .curWeek("2024-9-1 00:00:00")
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

        // 初始化 WeekView
        weekView
            .curWeek(timetableView.curWeek())
            .isShow(true)
            .showView()

            .callback(object : IWeekView.OnWeekItemClickedListener {
                @Override
                override fun onWeekClicked(week: Int) {
                    val cur = timetableView.curWeek()
                    //更新切换后的日期，从当前周cur->切换的周week
                    timetableView.onDateBuildListener()
                        .onUpdateDate(cur, week)
                    timetableView.changeWeekOnly(week)
                    curDisplayWeek = week

                    courseWeek.text = "第${curDisplayWeek}周"
                }
            })

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
        timetableView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event) // 捕获滑动手势
            false
        }

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
                intent.putExtra("curWeek", curDisplayWeek)
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
            Gson().fromJson(
                MMKV.defaultMMKV().decodeString(key, null),
                Array<MySubject>::class.java
            )
                .toMutableList()
        } catch (e: NullPointerException) {
            e.printStackTrace()
            nullTagSubjects
        }

    //    private fun ClickWheel(item:List<String>){
//
//        val Wheel = TimetableWheelBottomDialog(timeTableStore)
//        Wheel.setItem(item)
//        Wheel.show(supportFragmentManager,"wheel")
//    }
//    private val  swipeThreshold by lazy { resources.displayMetrics.density * 100 }
//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        var startX: Float = 0f
//        when (event?.action) {
//            MotionEvent.ACTION_DOWN -> {
//                Toast.makeText(this, "down", Toast.LENGTH_SHORT).show()
//                startX = event.x
//            }
//
//            MotionEvent.ACTION_MOVE -> {
//                Toast.makeText(this, "move", Toast.LENGTH_SHORT).show()
//                val deltaX = event.x - startX
//
//                if (Math.abs(deltaX) > swipeThreshold) {
//                    if (deltaX > 0) {
//                        Toast.makeText(this, "moveToNext", Toast.LENGTH_SHORT).show()
//                        changeWeek(timetableView.curWeek() + 1)
//                    } else {
//                        Toast.makeText(this, "moveToPrev", Toast.LENGTH_SHORT).show()
//                        changeWeek(timetableView.curWeek() - 1)
//                    }
//                }
//
//            }
//        }
//        return true
//    }
    private val gestureDetector by lazy {
        GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100 // 最小滑动距离
            private val SWIPE_VELOCITY_THRESHOLD = 100 // 最小滑动速度

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null || e2 == null) return false

                val diffX = e2.x - e1.x // 横向滑动距离
                val diffY = e2.y - e1.y // 纵向滑动距离

                if (Math.abs(diffX) > Math.abs(diffY)) { // 判断是横向滑动
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            changeWeek(curDisplayWeek - 1) // 右滑，切换到上一周
                        } else {
                            changeWeek(curDisplayWeek + 1) // 左滑，切换到下一周
                        }
                        return true
                    }
                }
                return false
            }

        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let { gestureDetector.onTouchEvent(it) } // 传递触摸事件
        return super.onTouchEvent(event)
    }

    private fun changeWeek(newWeek: Int) {
        if (newWeek in 1..20) {
            timetableView.changeWeekOnly(newWeek) // 仅更新课表中的周次
            timetableView.onDateBuildListener()
                .onUpdateDate(timetableView.curWeek(), newWeek) // 更新日期栏
            curDisplayWeek = newWeek
            runOnUiThread {
                courseWeek.text = "第${curDisplayWeek}周"
            }


            Toast.makeText(this, "切换到第${curDisplayWeek}周", Toast.LENGTH_SHORT).show()
        }
    }


}