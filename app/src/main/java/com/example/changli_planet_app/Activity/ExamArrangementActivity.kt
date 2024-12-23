package com.example.changli_planet_app.Activity

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Activity.Action.ExamInquiryAction
import com.example.changli_planet_app.Activity.Store.ExamArrangementStore
import com.example.changli_planet_app.Adapter.ExamArrangementAdapter
import com.example.changli_planet_app.Adapter.ExamScoreAdapter
import com.example.changli_planet_app.Data.jsonbean.Exam
import com.example.changli_planet_app.Data.jsonbean.ExamScore
import com.example.changli_planet_app.Network.Response.ExamArrangement
import com.example.changli_planet_app.R
import com.example.changli_planet_app.UI.ExamChosenDialog
import com.example.changli_planet_app.databinding.ActivityExamArrangementBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.w3c.dom.Text
import java.util.Calendar

class ExamArrangementActivity : AppCompatActivity() {
    lateinit var binding: ActivityExamArrangementBinding
    private val examRecyclerView: RecyclerView by lazy { binding.examRecyclerView }
    private val backImageView: ImageView by lazy { binding.homeBack }
    private val chosenTime: TextView by lazy { binding.chosenTime }
    private val semesterNumberDate: TextView by lazy { binding.semesterNumberDate }
    private val semesterDate: TextView by lazy { binding.semesterDate }
    private val progressBar: ProgressBar by lazy { binding.loadingProgress }
    private val store: ExamArrangementStore = ExamArrangementStore()
    private val currencyTime = generateTermsList()
    private var examList: MutableList<Exam> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExamArrangementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        examRecyclerView.layoutManager = LinearLayoutManager(this)
        examRecyclerView.adapter = ExamArrangementAdapter(examList)

        store.state()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { state ->
                showAllExamInfo(state.exams)
            }

        progressBar.visibility = View.VISIBLE
        store.dispatch(
            ExamInquiryAction.UpdateExamData(
                semesterNumberDate.text.toString(),
                semesterDate.text.toString()
            )
        )
        backImageView.setOnClickListener { finish() }
        chosenTime.setOnClickListener { showChosenDialog() }
    }

    private fun showAllExamInfo(exams: List<ExamArrangement>) {
        examList = exams.map { grade ->
            Exam(
                grade.name,
                grade.time,
                grade.examId,
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
        progressBar.visibility = View.GONE
    }

    private fun showChosenDialog() {
        val dialog = ExamChosenDialog.newInstance(currencyTime)
        dialog.setOnExamChosenListener { semester, examType ->
            if (semester.isNotEmpty() && examType.isNotEmpty()) {
                semesterNumberDate.text = semester
                semesterDate.text = examType
                progressBar.visibility = View.VISIBLE
                store.dispatch(
                    ExamInquiryAction.UpdateExamData(
                        semesterNumberDate.text.toString(),
                        semesterDate.text.toString()
                    )
                )
            }
        }
        dialog.show(supportFragmentManager, "ExamChosenDialog")
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
}