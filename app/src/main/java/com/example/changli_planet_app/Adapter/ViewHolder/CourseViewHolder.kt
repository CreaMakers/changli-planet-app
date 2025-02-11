package com.example.changli_planet_app.Adapter.ViewHolder

import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Data.model.CourseScore
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

        }
    }
}