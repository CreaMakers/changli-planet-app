package com.example.changli_planet_app.Feature.common.ui

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Activity.Action.ScoreInquiryAction
import com.example.changli_planet_app.Activity.Store.ScoreInquiryStore
import com.example.changli_planet_app.Adapter.ExamScoreAdapter
import com.example.changli_planet_app.Cache.ScoreCache
import com.example.changli_planet_app.Cache.StudentInfoManager
import com.example.changli_planet_app.Core.FullScreenActivity
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.Data.model.CourseScore
import com.example.changli_planet_app.Data.model.SemesterGroup
import com.example.changli_planet_app.Network.Response.Grade
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.ActivityScoreInquiryBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable

/**
 * 成绩查询页面
 */
class ScoreInquiryActivity : FullScreenActivity() {
    private lateinit var binding: ActivityScoreInquiryBinding
    private val recyclerView: RecyclerView by lazy { binding.ScoreRecyclerView }
    private val refresh: ImageView by lazy { binding.refresh }
    private val examScoreAdapter by lazy { ExamScoreAdapter(store, this@ScoreInquiryActivity) }
    private val store = ScoreInquiryStore()
    private val back by lazy { binding.bindingBack }
    private val disposables by lazy { CompositeDisposable() }


    private val studentId by lazy { StudentInfoManager.studentId }
    private val studentPassword by lazy { StudentInfoManager.studentPassword }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScoreInquiryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar){ view, windowInsets->
            val insets=windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                insets.top,
                view.paddingRight,
                view.paddingBottom
            )
            WindowInsetsCompat.CONSUMED
        }
        setupRecyclerView()
        initListener()
        loadCachedData()
        initObserveState()
    }

    private fun showLoading() {
        binding.loadingLayout.visibility = View.VISIBLE
        binding.ScoreRecyclerView.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.loadingLayout.visibility = View.GONE
        binding.ScoreRecyclerView.visibility = View.VISIBLE
    }

    private fun initListener() {
        back.setOnClickListener { finish() }
        refresh.setOnClickListener { refreshData(true) }
    }

    private fun initObserveState() {
        disposables.add(
            store.state()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state ->
                    hideLoading()
                    showInfo(state.grades)
                }
        )
    }


    private fun setupRecyclerView() {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ScoreInquiryActivity)
            adapter = examScoreAdapter
            itemAnimator = DefaultItemAnimator().apply {
                changeDuration = 200
            }
        }
    }

    private fun loadCachedData() {
        val cachedGrades = ScoreCache.getGrades()
        if (cachedGrades != null) {
            showInfo(cachedGrades)
        } else {
            refreshData(forceUpdate = true)
        }
    }

    private fun refreshData(forceUpdate: Boolean = false) {
        if (studentId.isEmpty() || studentPassword.isEmpty()) {
            showMessage("请先绑定学号和密码")
            Route.goBindingUser(this@ScoreInquiryActivity)
            finish()
            return
        }

        if (forceUpdate || ScoreCache.getGrades() == null) {
            store.dispatch(
                ScoreInquiryAction.UpdateGrade(
                this,
                studentId,
                studentPassword,
                refresh = {refreshData(true)}
            ))
            showLoading()
        }
    }

    private fun showInfo(rawData: List<Grade>) {
        if (rawData.isEmpty()) {
            return
        }
        ScoreCache.saveGrades(rawData)
        val groupedData = rawData.groupBy { it.item }.toSortedMap(compareByDescending { it })
        val semesterGroups = groupedData.map { (semester, grades) ->

            val courseScores = grades.map { grade ->
                CourseScore(
                    name = grade.name,
                    score = grade.grade.toIntOrNull() ?: 0,
                    credit = grade.score.toDoubleOrNull() ?: 0.0,
                    earnedCredit = grade.point.toDoubleOrNull() ?: 0.0,
                    courseType = grade.attribute,
                    pscjUrl = grade.pscjUrl,
                    cookie = grade.cookie
                )
            }

            val semesterGPA = calculateGPA(courseScores)

            SemesterGroup(
                semesterName = semester,
                gpa = semesterGPA,
                cours = courseScores
            )
        }

        val allCourses = rawData.size // 课程总数
        val totalCredits = rawData.sumOf { it.score.toDoubleOrNull() ?: 0.0 } // 已修总学分
        val totalGPA = calculateOverallGPA(rawData) // 总体 GPA
        val averageScore = calculateAverageScore(rawData) // 平均学分绩点


        binding.apply {
            totalGpa.text = String.format("%.2f", totalGPA)
            avgScore.text = String.format("%.1f", averageScore)
            totalClass.text = allCourses.toString()
            totalScore.text = String.format("%.1f", totalCredits)
        }

        examScoreAdapter.submitList(semesterGroups)
    }

    // 计算单个学期的 GPA
    private fun calculateGPA(courses: List<CourseScore>): Double {
        val totalWeightedPoints = courses.sumOf {
            it.credit * it.earnedCredit
        }
        val totalCredits = courses.sumOf { it.credit }
        return if (totalCredits > 0) totalWeightedPoints / totalCredits else 0.0
    }

    // 计算总体 GPA
    private fun calculateOverallGPA(grades: List<Grade>): Double {
        val totalWeightedPoints = grades.sumOf {
            (it.score.toDoubleOrNull() ?: 0.0) * (it.point.toDoubleOrNull() ?: 0.0)
        }
        val totalCredits = grades.sumOf { it.score.toDoubleOrNull() ?: 0.0 }
        return if (totalCredits > 0) totalWeightedPoints / totalCredits else 0.0
    }

    // 计算平均学分绩点
    private fun calculateAverageScore(grades: List<Grade>): Double {
        val totalScore = grades.sumOf { it.grade.toDoubleOrNull() ?: 0.0 }
        return if (grades.isNotEmpty()) totalScore / grades.size else 0.0
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

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}