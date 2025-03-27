package com.example.changli_planet_app.Activity

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.Activity.Action.ClassInfoAction
import com.example.changli_planet_app.Activity.Store.ClassInfoStore
import com.example.changli_planet_app.Cache.StudentInfoManager
import com.example.changli_planet_app.Core.FullScreenActivity
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Widget.Dialog.ClassInfoBottomDialog
import com.example.changli_planet_app.Widget.Dialog.EmptyClassroomDialog
import com.example.changli_planet_app.Widget.Dialog.UserProfileWheelBottomDialog
import com.example.changli_planet_app.Widget.Picker.LessonPicker
import com.example.changli_planet_app.Widget.View.CustomToast
import com.example.changli_planet_app.databinding.ActivityClassInfoBinding
import com.zhuangfei.timetable.model.ScheduleSupport
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class ClassInfoActivity : FullScreenActivity() {
    private lateinit var binding: ActivityClassInfoBinding
    private val disposables by lazy { CompositeDisposable() }
    private val query by lazy { binding.classInfoQuery }
    private val back by lazy { binding.personProfileBack }

    private val classText by lazy { binding.classInfoClass }
    private val weekText by lazy { binding.classInfoWeek }
    private val dayText by lazy { binding.classInfoDay }
    private val regionText by lazy { binding.classInfoRegion }

    private val classLayout by lazy { binding.classInfoClassLayout }
    private val weekLayout by lazy { binding.classInfoWeekLayout }
    private val dayLayout by lazy { binding.classInfoDayLayout }
    private val regionLayout by lazy { binding.classInfoRegionLayout }

    private val store = ClassInfoStore()
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
        "第20周",
    )

    private val dayList = listOf(
        "星期一",
        "星期二",
        "星期三",
        "星期四",
        "星期五",
        "星期六",
        "星期天",
    )
    private val regionList = listOf(
        "金盆岭校区",
        "云塘校区"
    )

    private val courseTimeMap= mapOf(
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY,8)
            set(Calendar.MINUTE,0)
        } to 1,
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY,10)
            set(Calendar.MINUTE,10)
        } to 3,
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY,14)
            set(Calendar.MINUTE,0)
        } to 5,
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY,16)
            set(Calendar.MINUTE,10)
        } to 7,
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY,19)
            set(Calendar.MINUTE,30)
        } to 9,
    )

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

    val maxHeight by lazy {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels
        screenHeight / 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initObserve()
        initListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    private fun initObserve() {
        disposables.add(
            store.state()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state ->
                    classText.text = "第${state.start}节到第${state.end}节"
                    weekText.text = "第${state.week}周"
                    dayText.text = state.day
                    regionText.text = state.region
                }
        )
    }

    private fun initView() {
        binding = ActivityClassInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        store.dispatch(ClassInfoAction.initilaize)

        val calendar=Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"))
        initWeek(calendar)
        initDay(calendar)
        initStartAndEnd(calendar)
    }

    private fun initWeek(calendar: Calendar){
        val startTerm=getCurTerm(calendar)
        startTerm?.let {
            val week=ScheduleSupport.timeTransfrom(termMap[startTerm])
            store.dispatch(ClassInfoAction.UpdateWeek("$week"))
        }
    }

    private fun initDay(calendar: Calendar){
        val day=calendar.get(Calendar.DAY_OF_WEEK)
        val realDay=dayList[(day+5)%7]                           //day的星期天是1，进行转换从星期一开始
        store.dispatch(ClassInfoAction.UpdateDay(realDay))
    }

    private fun initStartAndEnd(calendar: Calendar){
        for((key,value) in courseTimeMap){
            val hour1=key.get(Calendar.HOUR_OF_DAY)
            val minute1=key.get(Calendar.MINUTE)

            val hour2=calendar.get(Calendar.HOUR_OF_DAY)
            val minute2=calendar.get(Calendar.MINUTE)
            if(hour2<hour1||hour2==hour1&&minute2<=minute1){
                store.dispatch(ClassInfoAction.UpdateStartAndEnd(value.toString(),(value+1).toString()))//如果当前时间在这节大课前面显示这节大课的节次
                return
            }
        }
        val day=calendar.get(Calendar.DAY_OF_WEEK)
        val nextDay=dayList[(day+5+1)%7]                //如果在19：30以后就显示明天
        store.dispatch(ClassInfoAction.UpdateDay(nextDay))
    }

    private fun initListener() {
        back.setOnClickListener { finish() }
        weekLayout.setOnClickListener {
            clickWheel(weekList)
        }
        dayLayout.setOnClickListener {
            clickWheel(dayList)
        }
        regionLayout.setOnClickListener {
            clickWheel(regionList)
        }
        classLayout.setOnClickListener {
            val lessonPicker = LessonPicker(this)
            lessonPicker.setOnLessonSelectedListener { start, end ->
                store.dispatch(ClassInfoAction.UpdateStartAndEnd(start.toString(), end.toString()))
            }
            lessonPicker.show()
        }
        query.setOnClickListener {
            if (StudentInfoManager.studentId.isEmpty() || StudentInfoManager.studentPassword.isEmpty()) {
                CustomToast.showMessage(this, "请先绑定学号和密码")
                Route.goBindingUser(this)
                finish()
                return@setOnClickListener
            }
            store.dispatch(
                ClassInfoAction.QueryEmptyClassInfo(
                    this,
                    getCurTerm(Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai")))
                )
            )
        }
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

    private fun clickWheel(item: List<String>) {
        val wheel = ClassInfoBottomDialog(
            maxHeight, {
                store.dispatch(ClassInfoAction.UpdateWeek(it))
            },
            {
                store.dispatch(ClassInfoAction.UpdateDay(it))
            }, {
                store.dispatch(ClassInfoAction.UpdateRegion(it))
            }
        )
        wheel.setItem(item)
        wheel.show(supportFragmentManager, "ClassInfoWheel")
    }
}