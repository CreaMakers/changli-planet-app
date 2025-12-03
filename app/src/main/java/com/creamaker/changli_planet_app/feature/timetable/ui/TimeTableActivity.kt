package com.creamaker.changli_planet_app.feature.timetable.ui

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.TimeTableAppWidget
import com.creamaker.changli_planet_app.base.FullScreenActivity
import com.creamaker.changli_planet_app.common.cache.CommonInfo
import com.creamaker.changli_planet_app.common.data.local.mmkv.StudentInfoManager
import com.creamaker.changli_planet_app.core.MainActivity
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.core.network.ApiResponse
import com.creamaker.changli_planet_app.databinding.ActivityTimeTableBinding
import com.creamaker.changli_planet_app.databinding.CourseinfoDialogBinding
import com.creamaker.changli_planet_app.feature.common.data.local.entity.TimeTableMySubject
import com.creamaker.changli_planet_app.feature.common.listener.ScrollController
import com.creamaker.changli_planet_app.feature.ledger.ui.AddCourseActivity
import com.creamaker.changli_planet_app.feature.timetable.viewmodel.TimeTableViewModel
import com.creamaker.changli_planet_app.skin.SkinManager
import com.creamaker.changli_planet_app.utils.dp2Px
import com.creamaker.changli_planet_app.widget.dialog.ErrorStuPasswordResponseDialog
import com.creamaker.changli_planet_app.widget.dialog.NormalResponseDialog
import com.creamaker.changli_planet_app.widget.dialog.TimetableWheelBottomDialog
import com.creamaker.changli_planet_app.widget.view.ScrollTimeTableView
import com.google.android.material.snackbar.Snackbar
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class TimeTableActivity : FullScreenActivity<ActivityTimeTableBinding>() {

    private val viewModel: TimeTableViewModel by viewModels()
    private val mmkv by lazy { MMKV.defaultMMKV() }
    private val studentId by lazy { StudentInfoManager.studentId }
    private val studentPassword by lazy { StudentInfoManager.studentPassword }
    private val timetableView: ScrollTimeTableView by lazy { binding.timetableView }

    private lateinit var termList: List<String>
    private val weekList by lazy {
        (1..20).map { "第${it}周" }
    }

    override fun createViewBinding(): ActivityTimeTableBinding =
        ActivityTimeTableBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        resources
        // 检查用户绑定
        if (studentId.isEmpty() || studentPassword.isEmpty()) {
            showMessage("请先绑定学号和密码")
            Route.goBindingUser(this)
            finish()
            return
        }

        // 初始化
        initViewModel()
        initView()
        initTimetable()
        initObservers()
        setBack()

        // 首次对话框
        if (mmkv.getBoolean("isFirstDialog", true)) {
            NormalResponseDialog(
                this,
                "喵呜~ 试试左右滑动日期栏来切换周次吧，手机小组件功能也上线啦！(◍•ᴗ•◍)✧*",
                "贴心小提示"
            ).show()
            mmkv.encode("isFirstDialog", false)
        }

        // 设置学期列表
        setTermListById(studentId)

        // 加载课程数据
        val currentTerm = viewModel.getCurrentTerm()
        binding.courseTerm.text = currentTerm
        val currentWeek = viewModel.getCurWeek(currentTerm)
        viewModel.selectWeek("第${currentWeek}周")
        viewModel.selectTerm(currentTerm)
    }

    private fun initViewModel() {
        viewModel.initFirstLaunch()
    }

    private fun initView() {
        binding.headerLayout.courseRefresh.setOnClickListener {
            val currentTerm = binding.courseTerm.text.toString()
            viewModel.loadCourses(currentTerm, forceRefresh = true)
        }
        binding.courseWeek.setOnClickListener {
            showWheelDialog(weekList)
        }
        binding.weeksExtendBtn.setOnClickListener {
            showWheelDialog(weekList)
        }
        binding.courseTerm.setOnClickListener {
            showWheelDialog(termList)
        }
    }

    private fun initTimetable() {
        timetableView
            .maxSlideItem(10)
            .cornerAll(15)
            .itemHeight(dp2Px(this, 63))
            .isShowNotCurWeek(false)
            .showView()

        timetableView.apply {
            setBackgroundColor(getSkinColor(R.color.color_bg_primary))
            showTime()
            showPopDialog()
            buildItemText()
            addCourse()
            longClickToDeleteCourse()
            setScrollListener()
        }

        initTimetableDate()
    }

    @SuppressLint("SetTextI18n")
    private fun initObservers() {
        // UI State 观察
        viewModel.uiState.observe(this) { state ->
            timetableView.updateCurWeek()
            timetableView.source(state.subjects)
            timetableView.updateView()

            binding.courseTerm.text = state.term
            binding.courseWeek.text = state.weekInfo

            val displayWeek = extractWeekNumber(state.weekInfo)
            timetableView.changeWeekOnly(displayWeek)
            timetableView.onDateBuildListener()
                .onUpdateDate(timetableView.curWeek(), displayWeek)
            binding.isCurWeek.text = if (displayWeek == timetableView.curWeek()) {
                "本周"
            } else {
                "非本周"
            }
            Log.d("TimeTableActivity", "State updated: term=${state.term}, subjects=${state.subjects.size}")
        }

        // Courses Response 观察
        viewModel.coursesResponse.observe(this) { response ->
            when (response) {
                is ApiResponse.Loading -> {
                    showLoading()
                }
                is ApiResponse.Success -> {
                    hideLoading()
                    showMessage("加载成功")
                }
                is ApiResponse.Error -> {
                    hideLoading()
                    handleError(response.msg)
                }
            }
        }

        // Add Course Response 观察
        viewModel.addCourseResponse.observe(this) { response ->
            when (response) {
                is ApiResponse.Loading -> {
                    // 可选：显示添加中的提示
                }
                is ApiResponse.Success -> {
                    showMessage("添加课程成功")
                }
                is ApiResponse.Error -> {
                    showMessage(response.msg)
                }
            }
        }

        // Delete Course Response 观察
        viewModel.deleteCourseResponse.observe(this) { response ->
            when (response) {
                is ApiResponse.Loading -> {
                    // 可选：显示删除中的提示
                }
                is ApiResponse.Success -> {
                    showMessage("删除课程成功")
                }
                is ApiResponse.Error -> {
                    showMessage(response.msg)
                }
            }
        }

        // Current Display Week 观察
        viewModel.curDisplayWeek.observe(this) { week ->
            Log.d("TimeTableActivity", "Display week changed: $week")
        }
    }

    private fun handleError(message: String) {
        when {
            message.contains("学号") || message.contains("密码") -> {
                ErrorStuPasswordResponseDialog(
                    this,
                    message,
                    "查询失败"
                ) {
                    val currentTerm = binding.courseTerm.text.toString()
                    viewModel.loadCourses(currentTerm, forceRefresh = true)
                }.show()
            }
            message.contains("网络") -> {
                NormalResponseDialog(
                    this,
                    message,
                    "网络错误"
                ).show()
            }
            else -> {
                showMessage(message)
            }
        }
    }

    private fun TimetableView.showTime() {
        val times = arrayOf(
            "8:00\n8:45", "8:55\n9:40", "10:10\n10:55", "11:05\n11:50",
            "14:00\n14:45", "14:55\n15:40", "16:10\n16:55", "17:05\n17:50",
            "19:30\n20:15", "20:25\n21:10"
        )
        val slideBuildAdapter = OnSlideBuildAdapter()

        slideBuildAdapter
            .setTimes(times)
            .setTextColor(getSkinColor(R.color.color_text_primary))
            .setTimeTextColor(getSkinColor(R.color.color_text_highlight))   // ← 修改字体颜色
            .setBackground(getSkinColor(R.color.color_bg_primary))           // ← 新增：修改背景颜色

        callback(slideBuildAdapter)
        updateSlideView()
    }

    private fun TimetableView.showPopDialog() {
        callback(object : OnItemClickAdapter() {
            override fun onItemClick(v: View, scheduleList: MutableList<Schedule>) {
                val currentWeek = viewModel.curDisplayWeek.value ?: 1
                if (scheduleList.size == 1) {
                    showCourseDetailDialog(scheduleList.last())
                } else {
                    scheduleList.forEach {
                        if (currentWeek in it.weekList) {
                            showCourseDetailDialog(it)
                        }
                    }
                }
            }
        })
    }

    private fun TimetableView.buildItemText() {
        callback(object : OnItemBuildAdapter() {
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
                        append(schedule.name)
                        append("\n")
                        append("@${schedule.room}")
                        append("\n")
                        val teacherStart = length
                        append(schedule.teacher)
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
        }).updateView()
    }

    private fun TimetableView.addCourse() {
        hideFlaglayout()
        callback(object : OnFlaglayoutClickAdapter() {
            override fun onFlaglayoutClick(day: Int, start: Int) {
                val currentWeek = viewModel.curDisplayWeek.value ?: 1
                val currentTerm = viewModel.uiState.value?.term ?: ""
                val intent = Intent(context, AddCourseActivity::class.java).apply {
                    putExtra("day", day + 1)
                    putExtra("start", start)
                    putExtra("curWeek", currentWeek)
                    putExtra("curTerm", currentTerm)
                }
                startActivityForResult(intent, REQUEST_ADD_COURSE)
            }
        })
    }

    private fun TimetableView.longClickToDeleteCourse() {
        callback(object : OnItemLongClickAdapter() {
            override fun onLongClick(v: View, day: Int, start: Int) {
                val currentWeek = viewModel.curDisplayWeek.value ?: 1
                val currentTerm = viewModel.uiState.value?.term ?: ""

                val snackbar = Snackbar.make(v, "删除自定义课程", Snackbar.LENGTH_SHORT)
                snackbar.setAction("确定") {
                    viewModel.deleteCourse(day, start, currentWeek, currentTerm)
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

    private fun setScrollListener() {
        timetableView.setScrollInterface(object : ScrollController {
            override fun onScrollLast() {
                val currentWeek = viewModel.curDisplayWeek.value ?: 1
                if (currentWeek > 1) {
                    viewModel.selectWeek("第${currentWeek - 1}周")
                }
            }

            override fun onScrollNext() {
                val currentWeek = viewModel.curDisplayWeek.value ?: 1
                if (currentWeek < 20) {
                    viewModel.selectWeek("第${currentWeek + 1}周")
                }
            }
        })
    }

    private fun initTimetableDate() {
        timetableView.callback(object : ISchedule.OnDateBuildListener {
            private val layouts = arrayOfNulls<LinearLayout>(8)
            private lateinit var views: Array<View>

            override fun onInit(layout: LinearLayout?, alpha: Float) {
                layout?.alpha = alpha
            }

            @SuppressLint("SetTextI18n")
            override fun getDateViews(
                mInflate: LayoutInflater?,
                monthWidth: Float,
                perWidth: Float,
                height: Int
            ): Array<View> {
                val heightPx = 130
                views = Array(8) { TextView(this@TimeTableActivity) }

                val firstParams = LinearLayout.LayoutParams(monthWidth.toInt(), heightPx)
                val firstView = TextView(this@TimeTableActivity).apply {
                    layoutParams = firstParams
                    gravity = Gravity.CENTER
                    text = "${Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"))
                        .get(Calendar.MONTH) + 1}月"
                    setTextColor(getSkinColor(R.color.color_text_primary))      // ← 字体颜色
                    setBackgroundColor(getSkinColor(R.color.color_bg_primary))  // ← 背景颜色
                    textSize = 14f
                    typeface = Typeface.DEFAULT_BOLD
                }
                layouts[0] = null
                views[0] = firstView

                val weekParams = LinearLayout.LayoutParams(perWidth.toInt(), heightPx)
                weekParams.gravity = Gravity.CENTER
                val dateArray = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

                for (i in 1..7) {
                    val weekView = TextView(this@TimeTableActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        ).apply { gravity = Gravity.CENTER }
                        gravity = Gravity.CENTER
                        text = dateArray[i - 1]
                        setTextColor(getSkinColor(R.color.color_text_primary))          // ← 字体颜色
                        setBackgroundColor(getSkinColor(R.color.color_bg_primary))      // ← 背景颜色
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
                val defaultColor = getSkinColor(R.color.color_text_primary)
                val highlightColor = getSkinColor(R.color.color_base_red)
                for (i in 1..7) {
                    layouts[i]?.setBackgroundColor(defaultColor)
                }

                val today = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"))
                val currentTerm = binding.courseTerm.text.toString()
                val startTime = CommonInfo.termMap[currentTerm]

                startTime?.let {
                    val currentWeek = viewModel.curDisplayWeek.value ?: 1
                    val weekStart = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"))
                    weekStart.time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(it)!!
                    weekStart.add(Calendar.WEEK_OF_YEAR, currentWeek - 1)
                    weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

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
                val currentTerm = binding.courseTerm.text.toString()
                val startTime = CommonInfo.termMap[currentTerm]

                startTime?.let {
                    calendar.time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(it)!!
                    calendar.add(Calendar.WEEK_OF_YEAR, targetWeek - 1)
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

                    val today = Calendar.getInstance()
                    (views[0] as? TextView)?.text = "${calendar.get(Calendar.MONTH) + 1}月"

                    for (i in 1..7) {
                        val weekView = (layouts[i]?.getChildAt(0) as? TextView)
                        val day = calendar.get(Calendar.DAY_OF_MONTH)

                        val isToday = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                                calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)

                        weekView?.text = buildString {
                            append("周${getWeekDay(i)}")
                            append("\n")
                            append("${day}日")
                        }

                        if (isToday) {
                            weekView?.setTextColor(getSkinColor(R.color.color_text_highlight))
                        } else {
                            layouts[i]?.setBackgroundColor(getSkinColor(R.color.color_bg_primary))
                            weekView?.setTextColor(getSkinColor(R.color.color_text_primary))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_ADD_COURSE) {
            val courseJson = data?.getStringExtra("newCourse")
            courseJson?.let {
                val course = Gson().fromJson(it, TimeTableMySubject::class.java)
                viewModel.addCourse(course)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshWidget()
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
            schedule.weekList == allList.filter { it % 2 == 0 } ->
                "${schedule.weekList[0]} - ${schedule.weekList.last()} (双周)"
            schedule.weekList == allList.filter { it % 2 != 0 } ->
                "${schedule.weekList[0]} - ${schedule.weekList.last()} (单周)"
            schedule.weekList == allList ->
                "${schedule.weekList[0]} - ${schedule.weekList.last()} (周)"
            else -> schedule.weekList.joinToString(",") + "周"
        }
        schedule.room?.let { dialogBinding.dialogPlacepart.dialogPlace.text = it }

        val dialog = AlertDialog.Builder(this).apply {
            setView(dialogBinding.root)
        }.create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showLoading() {
        with(binding.headerLayout){
            courseRefreshLoading.visibility = View.VISIBLE
            courseRefresh.visibility = View.GONE
        }
    }

    private fun hideLoading() {
        with(binding.headerLayout){
            courseRefreshLoading.visibility = View.GONE
            courseRefresh.visibility = View.VISIBLE
        }
    }

    private fun showMessage(message: String) {
        val cardView = CardView(applicationContext).apply {
            radius = 25f
            cardElevation = 8f
            setCardBackgroundColor(getSkinColor(R.color.color_bg_secondary))
            useCompatPadding = true
        }

        val textView = TextView(applicationContext).apply {
            text = message
            textSize = 17f
            setTextColor(getSkinColor(R.color.color_text_primary))
            gravity = Gravity.CENTER
            setPadding(80, 40, 80, 40)
        }
        cardView.addView(textView)

        android.widget.Toast(applicationContext).apply {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 140)
            view = cardView
            duration = android.widget.Toast.LENGTH_SHORT
            show()
        }
    }

    private fun showWheelDialog(items: List<String>) {
        val maxHeight = resources.displayMetrics.heightPixels / 2
        val dialog = TimetableWheelBottomDialog(
            this,
            studentId,
            studentPassword,
            viewModel,
            maxHeight
        ) {
            val currentTerm = binding.courseTerm.text.toString()
            viewModel.loadCourses(currentTerm, forceRefresh = true)
        }
        dialog.setItem(items)
        dialog.show(supportFragmentManager, "TimetableWheel")
    }

    private fun setTermListById(studentId: String) {
        val list = mutableListOf<String>()
        val startYear = studentId.substring(0, 4).toInt()

        for (i in 0..3) {
            list.add("${startYear + i}-${startYear + i + 1}-1")
            list.add("${startYear + i}-${startYear + i + 1}-2")
        }
        termList = list
    }

    private fun extractWeekNumber(weekString: String): Int {
        val regex = Regex("\\d+")
        return regex.find(weekString)?.value?.toInt() ?: 1
    }

    private fun TimetableView.updateCurWeek() {
        val week = viewModel.getCurWeek(binding.courseTerm.text.toString())
        timetableView.curWeek(week)
        onWeekChangedListener().onWeekChanged(week)
    }

    private fun setBack() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fromWidget = intent.getBooleanExtra("from_timeTable_widget", false)
                if (fromWidget) {
                    val intent = Intent(this@TimeTableActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                    finish()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun refreshWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, TimeTableAppWidget::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        if (appWidgetIds.isNotEmpty()) {
            val intent = Intent(this, TimeTableAppWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            sendBroadcast(intent)
        }
    }
    private fun getSkinColor(resId: Int): Int {
        val skinRes = SkinManager.skinResources
        val skinPkg = SkinManager.skinPackageName

        if (skinRes != null && !skinPkg.isNullOrEmpty()) {
            try {
                val resName = resources.getResourceEntryName(resId)
                val resType = resources.getResourceTypeName(resId)
                val skinId = skinRes.getIdentifier(resName, resType, skinPkg)

                if (skinId != 0) {
                    return skinRes.getColor(skinId, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // 回退使用宿主 App 的颜色
        return getColor(resId)
    }

    companion object {
        private const val REQUEST_ADD_COURSE = 1
    }
}