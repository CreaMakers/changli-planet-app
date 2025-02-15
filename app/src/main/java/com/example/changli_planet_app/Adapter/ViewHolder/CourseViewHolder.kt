package com.example.changli_planet_app.Adapter.ViewHolder

import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Data.model.CourseScore
import com.example.changli_planet_app.Widget.Dialog.ScoreDetailDialog
import com.example.changli_planet_app.databinding.ScoreItemCourseBinding

class CourseViewHolder(
    private val binding: ScoreItemCourseBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(courseScore: CourseScore) {
        binding.apply {
            tvCourseName.text = courseScore.name
            tvCourseName.text = courseScore.name
            tvScore.text = courseScore.score.toString()
            tvCredit.text = String.format("学分: %.1f", courseScore.credit)
            getCredit.text = String.format("绩点: %.1f", courseScore.earnedCredit)
            tvType.text = courseScore.courseType
        }

        binding.scoreItemLayout.setOnClickListener {
            val sb = StringBuilder()
            if (!courseScore.pscj.isNullOrEmpty()) {
                sb.append("平时成绩: ${courseScore.pscj}\n平时成绩比例: ${courseScore.pscjBL}\n")
            }
            if (!courseScore.sjcj.isNullOrEmpty()) {
                sb.append("上机成绩: ${courseScore.sjcj}\n上机成绩比例: ${courseScore.sjcjBL}\n")
            }
            if (!courseScore.qzcj.isNullOrEmpty()) {
                sb.append("期中成绩: ${courseScore.qzcj}\n期中成绩比例: ${courseScore.qzcjBL}\n")
            }
            if (!courseScore.qmcj.isNullOrEmpty()) {
                sb.append("期末成绩: ${courseScore.qmcj}\n期末成绩比例: ${courseScore.qmcjBL}\n")
            }
            if (sb.isNotEmpty()) {
                sb.append("-------------------\n总成绩: ${courseScore.score}")
            }
            if (sb.isNotEmpty()) {
                ScoreDetailDialog(PlanetApplication.appContext, sb.toString(), courseScore.name)
            } else {
                ScoreDetailDialog(PlanetApplication.appContext, "暂无成绩详细", courseScore.name)
            }
        }
    }
}