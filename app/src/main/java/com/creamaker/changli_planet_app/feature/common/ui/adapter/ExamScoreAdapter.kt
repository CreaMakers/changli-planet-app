package com.creamaker.changli_planet_app.feature.common.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.creamaker.changli_planet_app.databinding.ScoreItemSemesterBinding
import com.creamaker.changli_planet_app.feature.common.redux.store.ScoreInquiryStore
import com.creamaker.changli_planet_app.feature.common.ui.adapter.model.CourseListItem
import com.creamaker.changli_planet_app.feature.common.ui.adapter.model.SemesterGroup
import com.creamaker.changli_planet_app.feature.common.ui.adapter.vh.SemesterViewHolder

class ExamScoreAdapter(
    private val store: ScoreInquiryStore,
    private val context: Context
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var _items = emptyList<CourseListItem>()
    private val currentList: List<CourseListItem> get() = _items

    private val CourseViewHolderViewPool = RecyclerView.RecycledViewPool().apply {
        setMaxRecycledViews(0,12)
    }

    companion object {
        private const val TYPE_SEMESTER = 0
        private const val TYPE_COURSE = 1
    }

    fun submitList(semesters: List<SemesterGroup>) {
        val newItems = mutableListOf<CourseListItem>()

        semesters.forEach { semester ->
            val semesterItem = CourseListItem.SemesterItem(semester)
            newItems.add(semesterItem)
        }

        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = _items.size
            override fun getNewListSize() = newItems.size

            override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
                val oldItem = _items[oldPos]
                val newItem = newItems[newPos]
                return when {
                    oldItem is CourseListItem.SemesterItem && newItem is CourseListItem.SemesterItem ->
                        oldItem.semester.semesterName == newItem.semester.semesterName

                    else -> false
                }
            }

            override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
                return _items[oldPos] == newItems[newPos]
            }
        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        _items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int) = TYPE_SEMESTER

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ScoreItemSemesterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        binding.rvCourses.setRecycledViewPool(CourseViewHolderViewPool)
        return SemesterViewHolder(binding, store, context) { position -> toggleGroup(position) }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = currentList[position] as CourseListItem.SemesterItem
        (holder as SemesterViewHolder).bind(item)
    }

    override fun getItemCount(): Int = currentList.size

    private fun toggleGroup(position: Int) {
        val currentItems = currentList.toMutableList()
        val semesterItem = currentItems[position] as? CourseListItem.SemesterItem ?: return
        currentItems[position] = semesterItem.copy(isExpanded = !semesterItem.isExpanded)
        _items = currentItems
        notifyItemChanged(position)
    }
}