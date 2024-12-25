package com.example.changli_planet_app.Activity

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Activity.Action.ScoreInquiryAction
import com.example.changli_planet_app.Activity.Store.ScoreInquiryStore
import com.example.changli_planet_app.Adapter.ExamScoreAdapter
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.Data.jsonbean.ExamScore
import com.example.changli_planet_app.Network.Response.Grade
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.ActivityScoreInquiryBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import jp.wasabeef.blurry.Blurry
import java.util.Calendar

class ScoreInquiryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScoreInquiryBinding
    private val backgroundLayout: LinearLayout by lazy { binding.mainInfoLayout }
    private val recyclerView: RecyclerView by lazy { binding.examRecyclerView }
    private val dataTextView: TextView by lazy { binding.dateText }
    private val chosenTimeTextView: TextView by lazy { binding.chosenTime }
    private val downwardImageView: ImageView by lazy { binding.downwardBtn }
    private val progressBar: ProgressBar by lazy { binding.loadingProgress }
    private val refresh: ImageView by lazy { binding.refresh }
    private val back: ImageView by lazy { binding.homeBack }
    val store = ScoreInquiryStore()
    private var currentPopupWindow: PopupWindow? = null
    private val terms: List<String> by lazy { generateTermsList() }
    private var examScoreList: MutableList<ExamScore> = mutableListOf()


    private val sharePreferences by lazy {
        getSharedPreferences("user_info", Context.MODE_PRIVATE)
    }
    private val studentId by lazy { sharePreferences.getString("student_id", "") ?: "" }
    private val studentPassword by lazy { sharePreferences.getString("password", "") ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScoreInquiryBinding.inflate(layoutInflater)
        window.statusBarColor = getColor(R.color.score_bar)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
        }

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        store.state()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { state ->
                showAllScoreInfo(state.grades)
            }

        backgroundLayout.post {
            Blurry.with(this)
                .radius(25)
                .sampling(3)
                .color(Color.parseColor("#9950B4FF"))
                .async()
                .onto(backgroundLayout)
        }
        listOf(chosenTimeTextView, downwardImageView).forEach {
            it.setOnClickListener { showTermSelector() }
        }
        store.dispatch(ScoreInquiryAction.initilaize)
        recyclerView.layoutManager = LinearLayoutManager(this)
        back.setOnClickListener {
            finish()
        }
        refresh.setOnClickListener {
            if (dataTextView.text.equals("日期")) {
                showMessage("请选择查询时间")
            } else {
                if (studentId.isNotEmpty() && studentPassword.isNotEmpty()) {
                    progressBar.visibility = View.VISIBLE
                    store.dispatch(
                        ScoreInquiryAction.UpdateGrade(
                            studentId,
                            studentPassword,
                            dataTextView.text.toString()
                        )
                    )
                } else {
                    showMessage("请先绑定学号和密码")
                    Route.goBindingUser(this)
                    finish()
                }

            }
        }
    }

    private fun showAllScoreInfo(grades: List<Grade>) {
        examScoreList = grades.map { grade ->
            ExamScore(
                grade.name,
                grade.attribute,
                grade.point,
                grade.grade
            )
        }.toMutableList()
        recyclerView.adapter = ExamScoreAdapter(examScoreList)
        if (recyclerView.adapter == null) {
            recyclerView.adapter = ExamScoreAdapter(examScoreList)
        } else {
            (recyclerView.adapter as ExamScoreAdapter).apply {
                updateData(examScoreList)
                notifyDataSetChanged()
            }
        }
        progressBar.visibility = View.GONE
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

    private fun showTermSelector() {

        if (currentPopupWindow?.isShowing == true) {
            currentPopupWindow?.dismiss()
            currentPopupWindow = null
            return
        }

        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_term_selector, null)
        val popupWindow = PopupWindow(
            popupView,
            (200 * resources.displayMetrics.density).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        currentPopupWindow = popupWindow

        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow.elevation = 10f
        popupWindow.setOnDismissListener {
            currentPopupWindow = null
        }

        val location = IntArray(2)
        chosenTimeTextView.getLocationInWindow(location)
        popupWindow.showAsDropDown(
            chosenTimeTextView,
            0,
            0
        )

        val termsList = popupView.findViewById<ListView>(R.id.terms_list)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, terms)
        termsList.adapter = adapter
        termsList.setOnItemClickListener { _, _, position, _ ->
            dataTextView.text = terms[position]
            popupWindow.dismiss()
            progressBar.visibility = View.VISIBLE
            if (studentId.isNotEmpty() && studentPassword.isNotEmpty()) {
                store.dispatch(
                    ScoreInquiryAction.UpdateGrade(
                        studentId,
                        studentPassword,
                        terms[position]
                    )
                )
            } else {
                showMessage("请先绑定学号和密码")
                Route.goBindingUser(this)
                finish()
            }
        }
    }
}