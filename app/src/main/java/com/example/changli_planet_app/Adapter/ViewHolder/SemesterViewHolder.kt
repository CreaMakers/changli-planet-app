package com.example.changli_planet_app.Adapter.ViewHolder

import android.content.Context
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Activity.Store.ScoreInquiryStore
import com.example.changli_planet_app.Adapter.CourseAdapter
import com.example.changli_planet_app.Data.model.CourseListItem
import com.example.changli_planet_app.databinding.ScoreItemSemesterBinding


class SemesterViewHolder(
    private val binding: ScoreItemSemesterBinding,
    private val store: ScoreInquiryStore,
    private val context: Context,
    private val onItemClick: (Int) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    private val courseAdapter = CourseAdapter(store, context)

    init {
        itemView.setOnClickListener {
            itemView.isClickable = false
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onItemClick(position)
            }
            itemView.postDelayed({ itemView.isClickable = true }, 300)
        }

        // 初始化内部的 RecyclerView
        binding.rvCourses.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = courseAdapter
            isNestedScrollingEnabled = false
        }
    }

    fun bind(item: CourseListItem.SemesterItem) {
        binding.apply {
            tvSemesterName.text = item.semester.semesterName
            tvGpa.text = "GPA: ${item.semester.gpa}"
            tvGpa.text = String.format("GPA: %.2f", item.semester.gpa)

            val rotation = if (item.isExpanded) 180f else 0f
            if (ivExpand.rotation != rotation) {
                ivExpand.animate()
                    .rotation(rotation)
                    .setDuration(300)
                    .setInterpolator(FastOutSlowInInterpolator())
                    .start()
            } else {
                ivExpand.rotation = rotation
            }

            if (item.isExpanded) {
                rvCourses.visibility = View.VISIBLE
                courseAdapter.submitList(item.semester.cours)
            } else {
                rvCourses.visibility = View.GONE
            }
        }
    }
}