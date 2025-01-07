package com.example.changli_planet_app.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.Activity.Action.TimeTableAction
import com.example.changli_planet_app.Activity.Store.TimeTableStore
import com.example.changli_planet_app.Cache.Room.CoursesDataBase
import com.example.changli_planet_app.Cache.Room.MySubject
import com.example.changli_planet_app.Cache.StudentInfoManager
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.Data.jsonbean.GetCourse
import com.example.changli_planet_app.R
import com.example.changli_planet_app.UI.TimetableWheelBottomDialog
import com.example.changli_planet_app.databinding.ActivityTimeTableBinding
import com.example.changli_planet_app.databinding.CourseinfoDialogBinding
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.zhuangfei.timetable.TimetableView
import com.zhuangfei.timetable.listener.ISchedule
import com.zhuangfei.timetable.listener.OnFlaglayoutClickAdapter
import com.zhuangfei.timetable.listener.OnItemBuildAdapter
import com.zhuangfei.timetable.listener.OnItemClickAdapter
import com.zhuangfei.timetable.listener.OnItemLongClickAdapter
import com.zhuangfei.timetable.listener.OnScrollViewBuildAdapter
import com.zhuangfei.timetable.listener.OnSlideBuildAdapter
import com.zhuangfei.timetable.model.Schedule
import com.zhuangfei.timetable.model.ScheduleSupport
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
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
    private lateinit var dataBase: CoursesDataBase
    private val termList by lazy { generateTermsList() }
    private val timeTableStore: TimeTableStore by lazy {
        TimeTableStore(dataBase.courseDao())
    }

    private fun showLoading() {
        binding.loadingLayout.visibility = View.VISIBLE
        binding.timetableView.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.loadingLayout.visibility = View.GONE
        binding.timetableView.visibility = View.VISIBLE
    }

    private val termMap by lazy {
        mapOf(
            "2024-2025-2" to "2025-02-24 00:00:00",
            "2024-2025-1" to "2024-09-02 00:00:00",
            "2023-2024-2" to "2024-02-26 00:00:00",
            "2023-2024-1" to "2023-09-04 00:00:00",
            "2022-2023-2" to "2023-02-20 00:00:00",
            "2022-2023-1" to "2022-08-29 00:00:00",
            "2021-2022-2" to "2022-02-21 00:00:00",
            "2021-2022-1" to "2021-09-06 00:00:00",
            "2020-2021-2" to "2021-03-01 00:00:00",
            "2020-2021-1" to "2020-08-24 00:00:00",
            "2019-2020-2" to "2020-02-17 00:00:00",
            "2019-2020-1" to "2019-09-02 00:00:00",
        )
    }

    private fun getCurrentTerm(): String {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        return when {
            currentMonth >= 9 -> "$currentYear-${currentYear + 1}-1"  // 第一学期
            currentMonth >= 2 -> "${currentYear - 1}-${currentYear}-2"  // 第二学期
            else -> "${currentYear - 1}-${currentYear}-1"  // 上学年第一学期
        }
    }

    private val weekList by lazy {
        listOf(
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
    }
    lateinit var subjects: MutableList<MySubject>
    private val studentId by lazy { StudentInfoManager.studentId }
    private val studentPassword by lazy { StudentInfoManager.studentPassword }

    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (studentId.isEmpty() || studentPassword.isEmpty()) {
            showMessage("请先绑定学号和密码")
            Route.goBindingUser(this)
            finish()
            return
        }
        showLoading()
        dataBase = CoursesDataBase.getDatabase(PlanetApplication.appContext)
        if (mmkv.getBoolean("isFirstLaunch", true)) {
            CoroutineScope(Dispatchers.IO).launch {
                dataBase.clearAllTables()
                withContext(Dispatchers.Main) {
                    mmkv.encode("isFirstLaunch", false)
                }

            }
        }
        courseTerm.text = getCurrentTerm()
        timetableView.setCurWeek(termMap[courseTerm.text])
        timetableView
            .maxSlideItem(10)
            .cornerAll(15)
            .isShowNotCurWeek(false)
            .showView()

        if (TimeTableStore.curState.subjects != null) {
            TimeTableStore.curState.lastUpdate = getValueInMMKV("lastUpdate")
        }
        initTimetableDate()
        disposables.add(
            timeTableStore.state().subscribe { curState ->
                timetableView.setCurWeek(termMap[curState.term])
                subjects = curState.subjects
                timetableView.source(subjects)
                timetableView.updateView()
                courseWeek.text = curState.weekInfo
                timetableView.changeWeekOnly(extractWeekNumber(courseWeek.text.toString()))
                curDisplayWeek = extractWeekNumber(courseWeek.text.toString())
                timetableView.onDateBuildListener()
                    .onUpdateDate(timetableView.curWeek(), curDisplayWeek)
                if (curDisplayWeek == timetableView.curWeek()) {
                    isCurWeek.text = "本周"
                } else {
                    isCurWeek.text = "非本周"
                }
                hideLoading()
                Log.d("Debug", "startTime : ${termMap[curState.term]}")
                Log.d("Debug", "curWeek : ${timetableView.curWeek()}")
            }

        )
        timeTableStore.dispatch(
            TimeTableAction.selectTerm(
                this@TimeTableActivity,
                studentId,
                studentPassword,
                getCurTerm(Calendar.getInstance())
            )
        )
//        timeTableStore.dispatch(
//            TimeTableAction.getStartTime(
//                "2024-9-25",
//                studentId,
//                studentPassword
//            )
//        )
        timeTableStore.dispatch(TimeTableAction.selectWeek("第${timetableView.curWeek()}周"))

//        timeTableStore.dispatch(
//            TimeTableAction.FetchCourses(
//                this,
//                GetCourse(
//                    studentId,
//                    studentPassword,
//                    "",
//                    courseTerm.text.toString()
//                )
//            )
//        )
        timetableView.apply {
            showTime()     //显示侧边栏时间
            showPopDialog()//课程点击事件,出现弹窗显示课程信息
            buildItemText()//按要求显示课程信息
            addCourse()    //添加自定义课程
            longClickToDeleteCourse()
        }

        findViewById<ImageButton>(R.id.courseRefresh).setOnClickListener {
            TimeTableStore.curState.lastUpdate = 0
            showLoading()
            timeTableStore.dispatch(
                TimeTableAction.FetchCourses(
                    this,
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
        binding.courseWeek.setOnClickListener {

            ClickWheel(weekList)
        }
        binding.weeksExtendBtn.setOnClickListener {
            ClickWheel(weekList)
        }
        // 暂时关闭选择学期
//        binding.courseTerm.setOnClickListener {
//            ClickWheel(termList)
//        }
        timetableView.findViewById<View>(com.zhuangfei.android_timetableview.sample.R.id.contentPanel)
            .setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                true
            }
        timetableView.callback(object : OnScrollViewBuildAdapter() {
            override fun getScrollView(mInflate: LayoutInflater?): View {
                // 创建一个自定义的滚动视图
                val customScrollView = HorizontalScrollView(timetableView.context).apply {
                    // 添加触摸事件监听
                    setOnTouchListener { _, event ->
                        gestureDetector.onTouchEvent(event) // 将触摸事件传递给 GestureDetector
                        true // 消费事件
                    }
                }

                // 使用原本的课程内容视图
                val courseContentView = super.getScrollView(mInflate)

                // 将课程内容视图添加到自定义滚动视图中
                customScrollView.addView(courseContentView)

                return customScrollView
            }
        })
    }

    private fun getCurTerm(instance: Calendar): String {
        val month = instance.get(Calendar.MONTH) + 1
        var year = instance.get(Calendar.YEAR)
        when {
            month in 9..12 -> return "$year-${year + 1}-1"
            month == 1 -> return "${year - 1}-${year}-1"
            else -> return "${year - 1}-${year}-2"
        }
    }


    private fun showCourseDetailDialog(schedule: Schedule) {
        val dialogBinding = CourseinfoDialogBinding.inflate(layoutInflater)
        dialogBinding.dialogCourseName.text = schedule.name
        dialogBinding.dialogTeacherpart.dialogTeacher.text = schedule.teacher
        dialogBinding.dialogAlarmpart.dialogCourseStep.text =
            "${schedule.start} - ${(schedule.start - 1 + schedule.step)} 节"

        val first = schedule.weekList[0]
        val last = schedule.weekList.last()
        val allList = (first..last).toList()
        dialogBinding.dialogWeekpart.dialogWeek.text = when {
            schedule.weekList.size == 1 -> "${schedule.weekList.last()}(周)"
            schedule.weekList == allList.filter { it % 2 == 0 } -> "${schedule.weekList[0]} - ${schedule.weekList.last()} (双周)"
            schedule.weekList == allList.filter { it % 2 != 0 } -> "${schedule.weekList[0]} - ${schedule.weekList.last()} (单周)"
            schedule.weekList == allList -> "${schedule.weekList[0]} - ${schedule.weekList.last()} (周)"
            else -> schedule.weekList.joinToString(",") + "周"
        }
        schedule.room?.let { dialogBinding.dialogPlacepart.dialogPlace.text = it }
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
            .setTimeTextColor(Color.parseColor("#ADD8E6"))
        timetableView.callback(slideBuildAdapter)
        timetableView.updateSlideView()
    }

    fun TimetableView.showPopDialog() {

        timetableView.callback(object : OnItemClickAdapter() {
            override fun onItemClick(v: View, scheduleList: MutableList<Schedule>) {
                if (scheduleList.size == 1) {
                    showCourseDetailDialog(scheduleList.last())
                    return
                } else {
                    scheduleList.forEach {
                        if (curDisplayWeek in it.weekList) showCourseDetailDialog(it)
                    }
                }

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
                textView?.tag = schedule
                textView?.text = when {
                    schedule?.room != null -> SpannableStringBuilder().apply {
                        // 课程名称（加粗，大字）
                        append(schedule.name)
                        append("\n")

                        // 教室（加粗，大字）
                        append("@${schedule.room}")
                        append("\n")

                        // 教师名（普通字体，小字）
                        val teacherStart = length
                        append(schedule.teacher)
                        setSpan(
                            StyleSpan(Typeface.NORMAL),  // 普通字体
                            teacherStart,
                            length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        setSpan(
                            RelativeSizeSpan(0.85f),  // 字体缩小到 0.9 倍
                            teacherStart,
                            length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    else -> SpannableStringBuilder().apply {
                        append(schedule?.name ?: "")
                        append("\n")

                        val teacherStart = length
                        append(schedule?.teacher ?: "")
                        setSpan(
                            StyleSpan(Typeface.NORMAL),
                            teacherStart,
                            length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        setSpan(
                            RelativeSizeSpan(0.85f),
                            teacherStart,
                            length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
                textView?.apply {
                    textSize = 12f
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    gravity = Gravity.START
                    isSingleLine = false
                    ellipsize = TextUtils.TruncateAt.END
                    setPadding(5, 5, 5, 5)
                    typeface = Typeface.DEFAULT_BOLD
                    setLineSpacing(6f, 1f)
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
                intent.putExtra("curTerm", TimeTableStore.curState.term)
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

            override fun onLongClick(v: View, day: Int, start: Int) {
                val snackbar = Snackbar.make(v, "删除自定义课程", Snackbar.LENGTH_SHORT)
                snackbar.setAction("确定") {
                    timeTableStore.dispatch(
                        TimeTableAction.DeleteCourse(
                            day,
                            start,
                            curDisplayWeek,
                            TimeTableStore.curState.term
                        )
                    )
                }
                val params = snackbar.view.layoutParams as ViewGroup.MarginLayoutParams
                params.bottomMargin = resources.displayMetrics.heightPixels / 7
                params.width = resources.displayMetrics.widthPixels / 3 * 2
                params.leftMargin = resources.displayMetrics.widthPixels / 4
                snackbar.view.layoutParams = params
                snackbar.show()
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
        val Wheel = TimetableWheelBottomDialog(
            this@TimeTableActivity,
            studentId,
            studentPassword,
            timeTableStore
        )
        Wheel.setItem(item)
        Wheel.show(supportFragmentManager, "TimetableWheel")
    }

    // 初始化 GestureDetector
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
                                timeTableStore.dispatch(
                                    TimeTableAction.selectWeek(
                                        "第${curDisplayWeek - 1}周"
                                    )
                                ) // 右滑，切换到上一周
                        } else {
                            if (curDisplayWeek + 1 in 1..20)
                                timeTableStore.dispatch(
                                    TimeTableAction.selectWeek(
                                        "第${curDisplayWeek + 1}周"
                                    )
                                ) // 左滑，切换到下一周
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    // 判断触摸事件是否在课程区域
    private fun isTouchInCourseArea(event: MotionEvent): Boolean {
        val timetableView = findViewById<View>(R.id.timetableView)
        val location = IntArray(2)
        timetableView.getLocationOnScreen(location)

        val x = event.rawX
        val y = event.rawY

        val left = location[0].toFloat()
        val top = location[1].toFloat()
        val right = left + timetableView.width
        val bottom = top + timetableView.height

        return x in left..right && y in top..bottom
    }

    // 重写触摸事件分发
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            if (isTouchInCourseArea(it)) {
                gestureDetector.onTouchEvent(it) // 在课程区域时触发手势检测
            }
        }
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

    private fun showMessage(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).apply {
            val cardView = CardView(applicationContext).apply {
                radius = 25f
                cardElevation = 8f
                setCardBackgroundColor(getColor(R.color.score_bar))
                useCompatPadding = true
            }

            val textView = TextView(applicationContext).apply {
                text = message
                textSize = 17f
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
                setPadding(80, 40, 80, 40)
            }
            cardView.addView(textView)
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 140)
            view = cardView
            show()
        }
    }

    private fun TimetableView.setCurWeek(startTime: String?) {
        startTime?.let {
            val week = ScheduleSupport.timeTransfrom(it)
            val curWeekField = TimetableView::class.java.getDeclaredField("curWeek")
            curWeekField.isAccessible = true
            curWeekField.set(timetableView, week)
            onWeekChangedListener().onWeekChanged(week)
        }

    }

    private fun initTimetableDate() {
        timetableView.callback(object : ISchedule.OnDateBuildListener {
            private val layouts = arrayOfNulls<LinearLayout>(8)
            private lateinit var views: Array<View>

            override fun onInit(layout: LinearLayout?, alpha: Float) {
                layout?.alpha = alpha
            }

            override fun getDateViews(
                mInflate: LayoutInflater?,
                monthWidth: Float,
                perWidth: Float,
                height: Int
            ): Array<View> {
                val heightPx = 130
                views = Array(8) { TextView(this@TimeTableActivity) }

                // 第一个视图（月份）
                val firstParams = LinearLayout.LayoutParams(monthWidth.toInt(), heightPx)
                val firstView = TextView(this@TimeTableActivity).apply {
                    layoutParams = firstParams
                    gravity = Gravity.CENTER
                    text = "${Calendar.getInstance().get(Calendar.MONTH) + 1}月"
                    setTextColor(Color.BLACK)
                    textSize = 14f
                    typeface = Typeface.DEFAULT_BOLD
                }
                layouts[0] = null
                views[0] = firstView

                val weekParams = LinearLayout.LayoutParams(perWidth.toInt(), heightPx)
                weekParams.gravity = Gravity.CENTER
                val dateArray = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

                // 创建周一至周日的视图
                for (i in 1..7) {
                    val weekView = TextView(this@TimeTableActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        ).apply {
                            gravity = Gravity.CENTER
                        }
                        gravity = Gravity.CENTER
                        text = dateArray[i - 1]
                        setTextColor(Color.BLACK)
                        textSize = 12f
                        setLineSpacing(8f, 1f)
                        typeface = Typeface.DEFAULT_BOLD
                    }

                    val weekLayout = LinearLayout(this@TimeTableActivity).apply {
                        layoutParams = weekParams
                        gravity = Gravity.CENTER
                        orientation = LinearLayout.VERTICAL
                        addView(weekView)
                    }
                    layouts[i] = weekLayout
                    views[i] = weekLayout
                }

                return views
            }

            override fun onHighLight() {
                // 初始化背景色
                val defaultColor = Color.parseColor("#F4F8F8")
                val highlightColor = Color.parseColor("#BFF6F4")

                // 重置所有背景色
                for (i in 1..7) {
                    layouts[i]?.setBackgroundColor(defaultColor)
                }

                // 获取当前日期
                val today = Calendar.getInstance()
                val startTime = termMap[courseTerm.text.toString()]

                startTime?.let {
                    val weekStart = Calendar.getInstance()
                    weekStart.time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(it)
                    weekStart.add(Calendar.WEEK_OF_YEAR, curDisplayWeek - 1)
                    weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

                    // 遍历本周的每一天
                    for (i in 1..7) {
                        if (weekStart.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                            weekStart.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                            weekStart.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
                        ) {
                            layouts[i]?.setBackgroundColor(highlightColor)
                            break
                        }
                        weekStart.add(Calendar.DAY_OF_MONTH, 1)
                    }
                }
            }

            override fun onUpdateDate(curWeek: Int, targetWeek: Int) {
                val calendar = Calendar.getInstance()
                val startTime = termMap[courseTerm.text.toString()]

                startTime?.let {
                    calendar.time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(it)
                    calendar.add(Calendar.WEEK_OF_YEAR, targetWeek - 1)
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

                    // 获取今天的日期
                    val today = Calendar.getInstance()

                    // 更新月份显示
                    (views[0] as? TextView)?.text = "${calendar.get(Calendar.MONTH) + 1}月"

                    // 更新日期显示和高亮
                    for (i in 1..7) {
                        val weekView = (layouts[i]?.getChildAt(0) as? TextView)
                        val day = calendar.get(Calendar.DAY_OF_MONTH)

                        // 检查是否是今天
                        val isToday = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                                calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)

                        // 更新文本
                        weekView?.text = buildString {
                            append("周${getWeekDay(i)}")
                            append("\n")
                            append("${day}日")
                        }

                        // 设置背景色和文字颜色
                        if (isToday) {
                            layouts[i]?.setBackgroundColor(Color.parseColor("#BFF6F4"))
                            weekView?.setTextColor(Color.parseColor("#1E88E5"))
                        } else {
                            layouts[i]?.setBackgroundColor(Color.parseColor("#F4F8F8"))
                            weekView?.setTextColor(Color.BLACK)
                        }

                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                    }
                }
            }

            private fun getWeekDay(index: Int): String {
                return when (index) {
                    1 -> "一"
                    2 -> "二"
                    3 -> "三"
                    4 -> "四"
                    5 -> "五"
                    6 -> "六"
                    7 -> "日"
                    else -> ""
                }
            }
        })
    }

}

