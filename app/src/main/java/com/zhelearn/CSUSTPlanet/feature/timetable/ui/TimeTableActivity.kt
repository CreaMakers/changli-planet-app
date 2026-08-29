package com.zhelearn.CSUSTPlanet.feature.timetable.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zhelearn.CSUSTPlanet.R
import com.zhelearn.CSUSTPlanet.TimeTableAppWidget
import com.zhelearn.CSUSTPlanet.common.cache.CommonInfo
import com.zhelearn.CSUSTPlanet.common.data.local.mmkv.StudentInfoManager
import com.zhelearn.CSUSTPlanet.core.MainActivity
import com.zhelearn.CSUSTPlanet.core.Route
import com.zhelearn.CSUSTPlanet.core.network.ApiResponse
import com.zhelearn.CSUSTPlanet.core.theme.AppSkinTheme
import com.zhelearn.CSUSTPlanet.core.theme.AppTheme
import com.zhelearn.CSUSTPlanet.feature.common.data.local.entity.TimeTableMySubject
import com.zhelearn.CSUSTPlanet.feature.ledger.ui.AddCourseActivity
import com.zhelearn.CSUSTPlanet.feature.timetable.data.local.TimeTableSettingsStore
import com.zhelearn.CSUSTPlanet.feature.timetable.ui.compose.TimeTableComposeScreen
import com.zhelearn.CSUSTPlanet.feature.timetable.ui.compose.TimeTableCourseUi
import com.zhelearn.CSUSTPlanet.feature.timetable.ui.compose.TimeTableDayHeaderUi
import com.zhelearn.CSUSTPlanet.feature.timetable.ui.entity.TimeTableUiState
import com.zhelearn.CSUSTPlanet.feature.timetable.viewmodel.TimeTableViewModel
import com.zhelearn.CSUSTPlanet.widget.dialog.NormalResponseDialog
import com.zhelearn.CSUSTPlanet.widget.dialog.TimetableWheelBottomDialog
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar

class TimeTableActivity : AppCompatActivity() {

    private val viewModel: TimeTableViewModel by viewModels()
    private val mmkv by lazy { MMKV.defaultMMKV() }
    private val studentId by lazy { StudentInfoManager.studentId }
    private val studentPassword by lazy { StudentInfoManager.studentPassword }

    private lateinit var termList: List<String>
    private val weekList by lazy { (1..20).map { "第${it}周" } }

    private var uiState by mutableStateOf(TimeTableUiState())
    private var isRefreshing by mutableStateOf(false)
    private var detailCourse by mutableStateOf<TimeTableCourseUi?>(null)
    private var overlapCourses by mutableStateOf<List<TimeTableCourseUi>>(emptyList())
    private var pendingDeleteCourse by mutableStateOf<TimeTableCourseUi?>(null)
    private var displaySettings by mutableStateOf(TimeTableSettingsStore.load())

    private val addCourseLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val courseJson = result.data?.getStringExtra(AddCourseActivity.EXTRA_NEW_COURSE)
            courseJson?.let {
                val parsedCourse = runCatching {
                    Gson().fromJson(it, TimeTableMySubject::class.java)
                }.getOrNull()
                if (parsedCourse != null) {
                    val selectedWeekArray: IntArray = result.data
                        ?.getIntArrayExtra(AddCourseActivity.EXTRA_SELECTED_WEEKS)
                        ?: intArrayOf()
                    val selectedWeeks = buildSet<Int> {
                        for (week in selectedWeekArray) {
                            if (week in 1..20) add(week)
                        }
                    }.sorted()
                    val course = if (selectedWeeks.isNotEmpty()) {
                        parsedCourse.copy(weeks = selectedWeeks)
                    } else {
                        parsedCourse
                    }
                    viewModel.addCourse(course)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (studentId.isEmpty() || studentPassword.isEmpty()) {
            showMessage(getString(R.string.bind_notification))
            Route.goBindingUser(this)
            finish()
            return
        }

        viewModel.initFirstLaunch()
        setTermListById(studentId)
        initObservers()
        setBack()

        if (mmkv.getBoolean("isFirstDialog", true)) {
            NormalResponseDialog(
                this,
                "喵呜~ 手机小组件功能也上线啦！(◍•ᴗ•◍)✧*",
                "贴心小提示"
            ).show()
            mmkv.encode("isFirstDialog", false)
        }

        val currentTerm = viewModel.getCurrentTerm()
        val initialAnchorStartDate = CommonInfo.getTermStartDate(currentTerm)?.let {
            runCatching { LocalDate.parse(it.substring(0, 10)) }.getOrNull()
        }
        val currentWeek = getCurrentDisplayWeek(
            term = currentTerm,
            anchorStartDate = initialAnchorStartDate,
            sundayFirst = displaySettings.sundayFirst,
        )
        viewModel.selectWeek("第${currentWeek}周")
        viewModel.selectTerm(currentTerm)

        setContent {
            AppSkinTheme {
                Surface(color = AppTheme.colors.bgPrimaryColor) {
                    TimeTableRouteScreen()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    private fun TimeTableRouteScreen() {
        val state = uiState
        val displayWeek = extractWeekNumber(state.weekInfo)
        val term = state.term.ifBlank { viewModel.getCurrentTerm() }
        val termStartDate by viewModel.termStartDate.collectAsState()
        val anchorStartDate = remember(termStartDate) {
            termStartDate?.let {
                runCatching { LocalDate.parse(it.substring(0, 10)) }.getOrNull()
            }
        }
        val currentWeek = remember(term, anchorStartDate, displaySettings.sundayFirst) {
            getCurrentDisplayWeek(term, anchorStartDate, displaySettings.sundayFirst)
        }
        val termStarted = remember(term, anchorStartDate, displaySettings.sundayFirst) {
            hasDisplayTermStarted(term, anchorStartDate, displaySettings.sundayFirst)
        }
        val courses = remember(state.subjects) {
            state.subjects.map { it.toComposeUi() }
        }
        val responseState by viewModel.coursesResponse.collectAsState(initial = null)
        var showErrorDialog by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }

        LaunchedEffect(responseState) {
            if (responseState is ApiResponse.Error) {
                errorMessage = "喵呜，被异次元拦截啦！请检查一下网络连接和学期选项哦૮₍˃⤙˂₎ა"
                showErrorDialog = true
            }
        }

        TimeTableComposeScreen(
            termText = term,
            weekText = state.weekInfo,
            displayWeek = displayWeek,
            currentWeek = currentWeek,
            termStarted = termStarted,
            courses = courses,
            remark = state.remark,
            displaySettings = displaySettings,
            dateHeaderProvider = remember(term, anchorStartDate, displaySettings.sundayFirst) {
                { week ->
                    buildDayHeaders(term, week, anchorStartDate, displaySettings.sundayFirst)
                }
            },
            isRefreshing = isRefreshing,
            onRefreshClick = {
                viewModel.loadCourses(term, forceRefresh = true)
            },
            onTermClick = { showWheelDialog(termList, isWeekPicker = false) },
            onWeekClick = { showWheelDialog(weekList, isWeekPicker = true) },
            onWeekChange = { week ->
                viewModel.selectWeek("第${week.coerceIn(1, 20)}周")
            },
            onDisplaySettingsChange = { settings ->
                displaySettings = TimeTableSettingsStore.save(settings)
            },
            onEmptySlotClick = { day, startSection, courseWeek ->
                launchAddCourse(day, startSection, courseWeek, term)
            },
            onCourseClick = {
                overlapCourses = emptyList()
                detailCourse = it
            },
            onOverlapCoursesClick = { coursesInSlot ->
                if (coursesInSlot.isNotEmpty()) {
                    detailCourse = coursesInSlot.first()
                    overlapCourses = coursesInSlot.drop(1)
                }
            },
            onCourseLongClick = { course ->
                if (course.isCustom) {
                    pendingDeleteCourse = course
                } else {
                    showMessage("仅支持删除自定义课程")
                }
            },
        )

        detailCourse?.let { course ->
            CourseDetailDialog(course = course) {
                if (overlapCourses.isNotEmpty()) {
                    detailCourse = overlapCourses.first()
                    overlapCourses = overlapCourses.drop(1)
                } else {
                    detailCourse = null
                }
            }
        }

        pendingDeleteCourse?.let { course ->
            AlertDialog(
                onDismissRequest = { pendingDeleteCourse = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteCourse(course.id, term)
                            pendingDeleteCourse = null
                        }
                    ) {
                        Text(text = "删除")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDeleteCourse = null }) {
                        Text(text = stringResource(id = android.R.string.cancel))
                    }
                },
                title = { Text(text = "删除课程") },
                text = { Text(text = "确定删除该自定义课程吗？") },
                containerColor = AppTheme.colors.bgCardColor,
                titleContentColor = AppTheme.colors.primaryTextColor,
                textContentColor = AppTheme.colors.secondaryTextColor,
                properties = DialogProperties(dismissOnClickOutside = true),
            )
        }

        if (responseState is ApiResponse.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = true,
                        onClick = {}
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = AppTheme.colors.commonColor,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                confirmButton = {
                    TextButton(onClick = { showErrorDialog = false }) {
                        Text(text = "我知道了", color = AppTheme.colors.commonColor)
                    }
                },
                title = { Text(text = "加载失败") },
                text = { Text(text = errorMessage) },
                containerColor = AppTheme.colors.bgCardColor,
                titleContentColor = AppTheme.colors.primaryTextColor,
                textContentColor = AppTheme.colors.secondaryTextColor,
            )
        }
    }

    @Composable
    private fun CourseDetailDialog(
        course: TimeTableCourseUi,
        onDismiss: () -> Unit,
    ) {
        val dialogBlue = Color(0xFF7DB7E8)
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = Color(0xFFF7FBFF),
                shape = RoundedCornerShape(20.dp),
            ) {
                Column {
                    // 蓝色头部
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(dialogBlue)
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                    ) {
                        Text(
                            text = course.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 26.sp,
                        )
                    }

                    // 详情列表
                    Column(
                        modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DetailRow(label = "教师", value = course.teacher.ifBlank { "未知" })
                        DetailRow(label = "地点", value = course.room.ifBlank { "未填写" })
                        DetailRow(
                            label = "节次",
                            value = "${course.startSection}-${course.startSection + course.sectionSpan - 1}节",
                        )
                        DetailRow(label = "周次", value = formatWeeks(course.weeks))

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Button(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = dialogBlue,
                                    contentColor = Color.White,
                                ),
                                modifier = Modifier.height(38.dp),
                            ) {
                                Text(
                                    text = stringResource(id = android.R.string.ok),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun DetailRow(
        label: String,
        value: String,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEAF4FF))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "$label：",
                color = Color(0xFF6F8FAF),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = value,
                color = Color(0xFF1C2B3A),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }

    private fun launchAddCourse(day: Int, startSection: Int, displayWeek: Int, term: String) {
        val intent = Intent(this, AddCourseActivity::class.java).apply {
            putExtra("day", day)
            putExtra("start", startSection)
            putExtra("curWeek", displayWeek)
            putExtra("curTerm", term)
        }
        addCourseLauncher.launch(intent)
    }

    private fun initObservers() {
        viewModel.uiState.observe(this) { state ->
            uiState = state
        }

        viewModel.addCourseResponse.observe(this) { response ->
            when (response) {
                is ApiResponse.Loading -> Unit
                is ApiResponse.Success -> showMessage("添加课程成功")
                is ApiResponse.Error -> showMessage(response.msg)
            }
        }

        viewModel.deleteCourseResponse.observe(this) { response ->
            when (response) {
                is ApiResponse.Loading -> Unit
                is ApiResponse.Success -> showMessage("删除课程成功")
                is ApiResponse.Error -> showMessage(response.msg)
            }
        }
    }

    private fun showMessage(message: String) {
        val cardView = CardView(applicationContext).apply {
            radius = 25f
            cardElevation = 8f
            setCardBackgroundColor(getColor(R.color.color_bg_secondary))
            useCompatPadding = true
        }

        val textView = TextView(applicationContext).apply {
            text = message
            textSize = 16f
            setTextColor(getColor(R.color.color_text_primary))
            gravity = Gravity.CENTER
            setPadding(80, 40, 80, 40)
        }
        cardView.addView(textView)

        Toast(applicationContext).apply {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 140)
            view = cardView
            duration = Toast.LENGTH_SHORT
            show()
        }
    }

    private fun showWheelDialog(items: List<String>, isWeekPicker: Boolean) {
        val maxHeight = resources.displayMetrics.heightPixels / 2
        val dialog = TimetableWheelBottomDialog(
            this,
            studentId,
            studentPassword,
            viewModel,
            maxHeight,
            isWeekPicker,
        ) {
            val currentTerm = uiState.term.ifBlank { viewModel.getCurrentTerm() }
            viewModel.loadCourses(currentTerm, forceRefresh = true)
        }
        dialog.setItem(items)
        dialog.show(supportFragmentManager, "TimetableWheel")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildDayHeaders(
        term: String,
        targetWeek: Int,
        anchorStartDate: LocalDate?,
        sundayFirst: Boolean,
    ): Pair<String, List<TimeTableDayHeaderUi>> {
        val zoneId = ZoneId.of("Asia/Shanghai")
        val today = LocalDate.now(zoneId)
        val daysFromWeekStart = if (sundayFirst) {
            today.dayOfWeek.value % 7
        } else {
            today.dayOfWeek.value - 1
        }
        val currentDisplayWeekStart = today.minusDays(daysFromWeekStart.toLong())

        val displayWeekStart = if (anchorStartDate != null) {
            val firstDisplayWeekStart = if (sundayFirst) {
                anchorStartDate.minusDays((anchorStartDate.dayOfWeek.value % 7).toLong())
            } else {
                anchorStartDate
            }
            firstDisplayWeekStart.plusWeeks((targetWeek - 1).toLong())
        } else {
            val currentWeek = getCurrentDisplayWeek(term, null, sundayFirst)
            currentDisplayWeekStart.plusWeeks((targetWeek - currentWeek).toLong())
        }

        val displayWeekEnd = displayWeekStart.plusDays(6)
        val monthText = if (displayWeekStart.monthValue != displayWeekEnd.monthValue) {
            "${displayWeekStart.monthValue}月/${displayWeekEnd.monthValue}月"
        } else {
            "${displayWeekStart.monthValue}月"
        }
        val labels = if (sundayFirst) {
            listOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
        } else {
            listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
        }
        val headers = List(7) { index ->
            val currentDate = displayWeekStart.plusDays(index.toLong())
            TimeTableDayHeaderUi(
                weekdayLabel = labels[index],
                dayOfMonthLabel = "${currentDate.dayOfMonth}日",
                isToday = currentDate == today,
            )
        }

        return monthText to headers
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentDisplayWeek(
        term: String,
        anchorStartDate: LocalDate?,
        sundayFirst: Boolean,
    ): Int {
        if (!sundayFirst) return viewModel.getCurWeek(term)

        val today = LocalDate.now(ZoneId.of("Asia/Shanghai"))
        if (anchorStartDate == null) {
            val weekOffset = if (today.dayOfWeek.value == 7 && viewModel.hasTermStarted(term)) 1 else 0
            return (viewModel.getCurWeek(term) + weekOffset).coerceIn(1, 20)
        }

        val firstWeekSunday = anchorStartDate.minusDays((anchorStartDate.dayOfWeek.value % 7).toLong())
        val elapsedDays = ChronoUnit.DAYS.between(firstWeekSunday, today)
        if (elapsedDays < 0) return 1
        return (elapsedDays / 7 + 1).toInt().coerceIn(1, 20)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun hasDisplayTermStarted(
        term: String,
        anchorStartDate: LocalDate?,
        sundayFirst: Boolean,
    ): Boolean {
        if (!sundayFirst || anchorStartDate == null) return viewModel.hasTermStarted(term)

        val firstWeekSunday = anchorStartDate.minusDays((anchorStartDate.dayOfWeek.value % 7).toLong())
        return !LocalDate.now(ZoneId.of("Asia/Shanghai")).isBefore(firstWeekSunday)
    }

    private fun TimeTableMySubject.toComposeUi(): TimeTableCourseUi {
        return TimeTableCourseUi(
            id = if (id != 0) id else ("${courseName}_${weekday}_${start}_${step}".hashCode()),
            title = courseName,
            teacher = teacher,
            room = classroom.orEmpty(),
            dayOfWeek = weekday.coerceIn(1, 7),
            startSection = start.coerceIn(1, 10),
            sectionSpan = step.coerceAtLeast(1),
            weeks = (weeks ?: emptyList()).toSet(),
            isCustom = isCustom,
        )
    }

    private fun formatWeeks(weeks: Set<Int>): String {
        if (weeks.isEmpty()) return "未设置"
        val sorted = weeks.sorted()
        val first = sorted.first()
        val last = sorted.last()
        val full = (first..last).toList()
        return when {
            sorted.size == 1 -> "${sorted.first()}周"
            sorted == full -> "$first-${last}周"
            sorted == full.filter { it % 2 == 0 } -> "$first-${last}周(双周)"
            sorted == full.filter { it % 2 != 0 } -> "$first-${last}周(单周)"
            else -> sorted.joinToString(",") + "周"
        }
    }

    private fun setTermListById(studentId: String) {
        val startYear = studentId.take(4).toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
        termList = buildList {
            for (i in 0..3) {
                add("${startYear + i}-${startYear + i + 1}-1")
                add("${startYear + i}-${startYear + i + 1}-2")
            }
        }
    }

    private fun extractWeekNumber(weekString: String): Int {
        return Regex("\\d+").find(weekString)?.value?.toIntOrNull() ?: 1
    }

    private fun setBack() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fromWidget = intent.getBooleanExtra("from_timeTable_widget", false)
                if (fromWidget) {
                    val mainIntent = Intent(this@TimeTableActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(mainIntent)
                    finish()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        refreshWidget()
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
}
