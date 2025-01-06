package com.example.changli_planet_app.Activity

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Activity.Action.ExamInquiryAction
import com.example.changli_planet_app.Activity.Store.ExamArrangementStore
import com.example.changli_planet_app.Adapter.ExamArrangementAdapter
import com.example.changli_planet_app.Cache.ExamArrangementCache
import com.example.changli_planet_app.Cache.StudentInfoManager
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.Data.jsonbean.Exam
import com.example.changli_planet_app.Network.Response.ExamArrangement
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.ActivityExamArrangementBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.Calendar

class ExamArrangementActivity : AppCompatActivity() {
    lateinit var binding: ActivityExamArrangementBinding
    private val examRecyclerView: RecyclerView by lazy { binding.recyclerView }
    private val back: ImageView by lazy { binding.bindingBack }
    private val refresh: ImageView by lazy { binding.refresh }
    private val store: ExamArrangementStore = ExamArrangementStore()
    private var examList: MutableList<Exam> = mutableListOf()
    private val cache by lazy { ExamArrangementCache(this) }
    private val studentId by lazy { StudentInfoManager.studentId }
    private val studentPassword by lazy { StudentInfoManager.studentPassword }
    private val disposables by lazy { CompositeDisposable() }
    private fun showLoading() {
        binding.loadingLayout.visibility = View.VISIBLE
        examRecyclerView.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.loadingLayout.visibility = View.GONE
        examRecyclerView.visibility = View.VISIBLE
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExamArrangementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        examRecyclerView.layoutManager = LinearLayoutManager(this)
        examRecyclerView.adapter = ExamArrangementAdapter(examList)

        disposables.add(
            store.state()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state ->
                    showAllExamInfo(state.exams)
                }
        )


        loadCacheData()
        back.setOnClickListener { finish() }
        refresh.setOnClickListener { refreshData() }
    }

    private fun loadCacheData() {
        val examArrangement = cache.getExamArrangement()
        if (examArrangement != null) {
            showAllExamInfo(examArrangement)
        } else {
            refreshData()
        }
    }

    private fun refreshData() {
        if (studentId.isNotEmpty() && studentPassword.isNotEmpty()) {
            showLoading()
            store.dispatch(
                ExamInquiryAction.UpdateExamData(
                    this,
                    studentId,
                    studentPassword,
                    getCurrentTerm()
                )
            )
        } else {
            showMessage("请先绑定学号和密码")
            Route.goBindingUser(this@ExamArrangementActivity)
            finish()
        }
    }

    private fun showAllExamInfo(exams: List<ExamArrangement>) {
        if (exams.isEmpty()) {
            hideLoading()
            return
        }
        cache.saveExamArrangement(exams)
        examList = exams.map { grade ->
            Exam(
                grade.name,
                grade.time,
                grade.place,
                grade.room
            )
        }.toMutableList()
        examRecyclerView.adapter = ExamArrangementAdapter(examList)
        if (examRecyclerView.adapter == null) {
            examRecyclerView.adapter = ExamArrangementAdapter(examList)
        } else {
            (examRecyclerView.adapter as ExamArrangementAdapter).apply {
                updateData(examList)
                notifyDataSetChanged()
            }
        }
        hideLoading()
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