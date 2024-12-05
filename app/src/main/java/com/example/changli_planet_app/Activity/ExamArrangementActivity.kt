package com.example.changli_planet_app.Activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Adapter.ExamArrangementAdapter
import com.example.changli_planet_app.Data.jsonbean.Exam
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.ActivityExamArrangementBinding
import java.util.Calendar

class ExamArrangementActivity : AppCompatActivity() {
    lateinit var binding: ActivityExamArrangementBinding
    private val examRecyclerView: RecyclerView by lazy { binding.examRecyclerView }

    private val examList: List<Exam> = listOf(
        Exam(
            name = "通用工程英语读写",
            time = "2024-11-19 14:05~15:15",
            examId = "01002",
            room = "金1教3楼-7机房"
        ),
        Exam(
            name = "操作系统A",
            time = "2024-12-17 16:10~17:50",
            examId = "qm010149",
            room = "金12-306"
        ),
        Exam(
            name = "计算机网络原理与技术B",
            time = "2024-12-27 14:00~15:40",
            examId = "qm010198",
            room = "金12-115"
        )
    )
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