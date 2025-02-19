package com.example.changli_planet_app.Activity

import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.Activity.Action.ClassInfoAction
import com.example.changli_planet_app.Activity.Store.ClassInfoStore
import com.example.changli_planet_app.Core.FullScreenActivity
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Widget.Dialog.ClassInfoBottomDialog
import com.example.changli_planet_app.Widget.Dialog.EmptyClassroomDialog
import com.example.changli_planet_app.Widget.Dialog.UserProfileWheelBottomDialog
import com.example.changli_planet_app.Widget.Picker.LessonPicker
import com.example.changli_planet_app.databinding.ActivityClassInfoBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.Calendar

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
        "星期填",
    )
    private val regionList = listOf(
        "金盆岭校区",
        "云塘校区"
    )
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
            store.dispatch(
                ClassInfoAction.QueryEmptyClassInfo(
                    this,
                    getCurTerm(Calendar.getInstance())
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