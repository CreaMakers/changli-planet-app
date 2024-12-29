package com.example.changli_planet_app.Activity

import java.lang.reflect.Type
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.Activity.Action.TimeTableAction
import com.example.changli_planet_app.Activity.Store.TimeTableStore
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Cache.Room.CoursesDataBase
import com.example.changli_planet_app.Data.jsonbean.GetCourse
import com.example.changli_planet_app.Cache.Room.MySubject
import com.example.changli_planet_app.Cache.UserInfoManager
import com.example.changli_planet_app.R
import com.example.changli_planet_app.UI.TimetableWheelBottomDialog
import com.example.changli_planet_app.databinding.ActivityTimeTableBinding
import com.example.changli_planet_app.databinding.CourseinfoDialogBinding
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.zhuangfei.timetable.TimetableView
import com.zhuangfei.timetable.listener.ISchedule
import com.zhuangfei.timetable.listener.OnFlaglayoutClickAdapter
import com.zhuangfei.timetable.listener.OnItemBuildAdapter
import com.zhuangfei.timetable.listener.OnItemClickAdapter
import com.zhuangfei.timetable.listener.OnItemLongClickAdapter
import com.zhuangfei.timetable.listener.OnSlideBuildAdapter
import com.zhuangfei.timetable.model.Schedule
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.Calendar


class TimeTableActivity : AppCompatActivity() {
    private val mmkv by lazy { MMKV.defaultMMKV() }
    private val disposables by lazy { CompositeDisposable() }
    private val isCurWeek by lazy { binding.isCurWeek }
    private val courseTerm by lazy { binding.courseTerm }
    private val courseWeek by lazy { binding.courseWeek }
    private var curDisplayWeek = 0
    private val binding by lazy { ActivityTimeTableBinding.inflate(layoutInflater) }
    private val timetableView: TimetableView by lazy { binding.timetableView }
    lateinit var dataBase: CoursesDataBase
    private val timeTableStore: TimeTableStore by lazy {
        TimeTableStore(dataBase.courseDao())
    }

    private val weekList = listOf(
        "第1周",
        "第2周",
        "第3周",
        "第4周",
        "第5周",
        "第6周",
        "第7周",
        "第8周",
        "第9周",
        "第10周",
        "第11周",
        "第12周",
        "第13周",
        "第14周",
        "第15周",
        "第16周",
        "第17周",
        "第18周",
        "第19周",
        "第20周"
    )
    lateinit var subjects: MutableList<MySubject>
    private val studentId by lazy { UserInfoManager.studentId }
    private val studentPassword by lazy { UserInfoManager.studentPassword }

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
        // 初始化 TimetableView
        timetableView
            .curWeek("2024-9-1 00:00:00")
            .maxSlideItem(10)
            .cornerAll(15)
            .isShowNotCurWeek(false)
            .showView()

        if (TimeTableStore.curState.subjects != null) {
            TimeTableStore.curState.lastUpdate = getValueInMMKV("lastUpdate")
        }
        disposables.add(
            timeTableStore.state().subscribe { curState ->
                subjects = curState.subjects
                timetableView.source(subjects)
                timetableView.updateView()
                courseWeek.text = curState.weekInfo
                courseTerm.text = curState.term
                timetableView.changeWeekOnly(extractWeekNumber(courseWeek.text.toString()))
                curDisplayWeek = extractWeekNumber(courseWeek.text.toString())
                timetableView.onDateBuildListener()
                    .onUpdateDate(timetableView.curWeek(), curDisplayWeek)
                if (curDisplayWeek == timetableView.curWeek()) {
                    isCurWeek.text = "本周"
                } else {
                    isCurWeek.text = "非本周"
                }
                Log.d("Debug", "Subjects in subscription: $subjects")
            }
        )

        timeTableStore.dispatch(
            TimeTableAction.FetchCourses(
                GetCourse(
                    studentId,
                    studentPassword,
                    "",
                    courseTerm.text.toString()
                )
            )
        )



        timeTableStore.dispatch(
            TimeTableAction.selectTerm(courseTerm.text.toString())
        )
        timeTableStore.dispatch(TimeTableAction.selectWeek("第${timetableView.curWeek()}周"))




        timetableView.apply {
            showTime()     //显示侧边栏时间
            showPopDialog()//课程点击事件,出现弹窗显示课程信息
            buildItemText()//按要求显示课程信息
            addCourse()    //添加自定义课程
            longClickToDeleteCourse()
        }

        findViewById<ImageButton>(R.id.courseRefresh).setOnClickListener {
            TimeTableStore.curState.lastUpdate = 0
            timeTableStore.dispatch(
                TimeTableAction.FetchCourses(
                    GetCourse(
                        studentId,
                        studentPassword,
                        "",
                        courseTerm.text.toString()
                    )
                )
            )
            timetableView.updateView()

        }

        binding.weeksExtendBtn.setOnClickListener {
            ClickWheel(weekList)
        }
//        binding.termsExtendBtn.setOnClickListener {
//            ClickWheel(termList)
//        }


        timetableView.viewTreeObserver.addOnGlobalLayoutListener {
            val scrollView = findScrollView(timetableView)
            scrollView?.setOnTouchListener(object : View.OnTouchListener {
                private var initialX = 0f
                private var initialY = 0f

                override fun onTouch(view: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            // 记录初始触摸点位置
                            initialX = event.x
                            initialY = event.y

                            // 允许父视图拦截
                            view.parent?.requestDisallowInterceptTouchEvent(false)
                        }

                        MotionEvent.ACTION_MOVE -> {
                            val deltaX = Math.abs(event.x - initialX)
                            val deltaY = Math.abs(event.y - initialY)

                            if (deltaX > deltaY) {
                                // 横向滑动，允许父视图处理
                                view.parent?.requestDisallowInterceptTouchEvent(false)
                            } else {
                                // 垂直滑动，不允许父视图处理
                                view.parent?.requestDisallowInterceptTouchEvent(true)
                            }
                        }
                    }
                    return false
                }
            })
        }

    }

    private fun findScrollView(view: View): ScrollView? {
        if (view is ScrollView) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                val result = findScrollView(child)
                if (result != null) return result
            }
        }
        return null
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
//        AlertDialog.Builder(this).apply {
//            setView(dialogBinding.root)
//            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            show()
//        }
        val dialog = AlertDialog.Builder(this).apply {
            setView(dialogBinding.root)
        }.create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

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
                countTextView?.visibility = View.GONE
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
                startActivityForResult(intent, 1)

            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 1) {
            val course = Gson().fromJson(data?.getStringExtra("newCourse"), MySubject::class.java)
            course?.let { timeTableStore.dispatch(TimeTableAction.AddCourse(it)) }
            subjects.add(course)
        }

    }

    fun TimetableView.longClickToDeleteCourse() {
        timetableView.callback(object : OnItemLongClickAdapter() {

            override fun onLongClick(v: View?, day: Int, start: Int) {

            }
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        storeInMMKV("lastUpdate", TimeTableStore.curState.lastUpdate)
        disposables.dispose()
    }

    private fun storeInMMKV(key: String, value: Long) = mmkv.encode(key, value)


    private fun getValueInMMKV(key: String) = mmkv.decodeLong(key)

    private fun ClickWheel(item: List<String>) {
        val Wheel = TimetableWheelBottomDialog(timeTableStore)
        Wheel.setItem(item)
        Wheel.show(supportFragmentManager, "TimetableWheel")
    }

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
                            if (curDisplayWeek - 1 in 1..20)
                                timeTableStore.dispatch(TimeTableAction.selectWeek("第${curDisplayWeek - 1}周")) // 右滑，切换到上一周
                        } else {
                            if (curDisplayWeek + 1 in 1..20)
                                timeTableStore.dispatch(TimeTableAction.selectWeek("第${curDisplayWeek + 1}周"))// 左滑，切换到下一周
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


    private fun generateTermsList(yearsBack: Int = 15): List<String> {
        val terms = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val startYear = when {
            currentMonth >= 9 -> currentYear
            else -> currentYear - 1
        }
        val currentTerm = when {
            currentMonth >= 9 -> 1
            currentMonth >= 2 -> 2
            else -> 1
        }
        for (year in startYear downTo (startYear - yearsBack)) {
            if (year == startYear) {
                if (currentTerm == 1) {
                    terms.add("$year-${year + 1}-1")
                } else {
                    terms.add("$year-${year + 1}-2")
                    terms.add("$year-${year + 1}-1")
                }
            } else {
                terms.add("$year-${year + 1}-2")
                terms.add("$year-${year + 1}-1")
            }
        }
        return terms
    }

    fun extractWeekNumber(weekString: String): Int {
        val regex = Regex("\\d+")
        val matchResult = regex.find(weekString)
        return matchResult?.value?.toInt() ?: 0
    }


}