package com.creamaker.changli_planet_app.widget.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.creamaker.changli_planet_app.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WeekMultiSelectBottomDialog(
    initialWeeks: Set<Int>,
    private val onConfirm: (Set<Int>) -> Unit,
) : BottomSheetDialogFragment() {

    private val selectedWeeks = linkedSetOf<Int>()
    private lateinit var weekAdapter: WeekChipAdapter
    private lateinit var summaryView: TextView
    private lateinit var confirmView: View

    init {
        selectedWeeks.addAll(initialWeeks.filter { it in 1..20 })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val root = inflater.inflate(R.layout.dialog_week_multi_select, container, false)
        summaryView = root.findViewById(R.id.tvWeekPickerSummary)
        confirmView = root.findViewById(R.id.btnWeekConfirm)

        root.findViewById<View>(R.id.ivWeekPickerClose).setOnClickListener { dismiss() }
        root.findViewById<View>(R.id.btnWeekCancel).setOnClickListener { dismiss() }

        root.findViewById<View>(R.id.btnWeekSelectAll).setOnClickListener {
            selectedWeeks.clear()
            selectedWeeks.addAll(1..20)
            weekAdapter.notifyDataSetChanged()
            refreshSummary()
        }
        root.findViewById<View>(R.id.btnWeekClear).setOnClickListener {
            selectedWeeks.clear()
            weekAdapter.notifyDataSetChanged()
            refreshSummary()
        }
        root.findViewById<View>(R.id.btnWeekOdd).setOnClickListener {
            selectedWeeks.clear()
            selectedWeeks.addAll((1..20).filter { it % 2 != 0 })
            weekAdapter.notifyDataSetChanged()
            refreshSummary()
        }
        root.findViewById<View>(R.id.btnWeekEven).setOnClickListener {
            selectedWeeks.clear()
            selectedWeeks.addAll((1..20).filter { it % 2 == 0 })
            weekAdapter.notifyDataSetChanged()
            refreshSummary()
        }

        root.findViewById<View>(R.id.btnWeekConfirm).setOnClickListener {
            onConfirm(selectedWeeks.toSet())
            dismiss()
        }

        val weekRecycler = root.findViewById<RecyclerView>(R.id.rvWeeks)
        weekAdapter = WeekChipAdapter(selectedWeeks) {
            refreshSummary()
        }
        weekRecycler.layoutManager = GridLayoutManager(context, 5)
        weekRecycler.adapter = weekAdapter

        refreshSummary()
        return root
    }

    private fun refreshSummary() {
        summaryView.text = if (selectedWeeks.isEmpty()) {
            "当前未选择周次"
        } else {
            "已选 ${selectedWeeks.size} 周"
        }
        confirmView.isEnabled = selectedWeeks.isNotEmpty()
        confirmView.alpha = if (selectedWeeks.isEmpty()) 0.55f else 1f
    }

    private class WeekChipAdapter(
        private val selectedWeeks: LinkedHashSet<Int>,
        private val onSelectionChanged: () -> Unit,
    ) : RecyclerView.Adapter<WeekChipAdapter.WeekChipViewHolder>() {

        private val weeks = (1..20).toList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekChipViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_week_chip, parent, false)
            return WeekChipViewHolder(view)
        }

        override fun getItemCount(): Int = weeks.size

        override fun onBindViewHolder(holder: WeekChipViewHolder, position: Int) {
            val week = weeks[position]
            val selected = selectedWeeks.contains(week)
            holder.weekView.text = "第${week}周"
            holder.weekView.isSelected = selected
            holder.weekView.isActivated = selected
            holder.weekView.setOnClickListener {
                if (selectedWeeks.contains(week)) {
                    selectedWeeks.remove(week)
                } else {
                    selectedWeeks.add(week)
                }
                notifyItemChanged(position)
                onSelectionChanged()
            }
        }

        class WeekChipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val weekView: TextView = itemView.findViewById(R.id.tvWeekChip)
        }
    }
}