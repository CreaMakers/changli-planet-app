package com.creamaker.changli_planet_app.widget.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.feature.common.ui.adapter.TimeTableSelectorAdapter
import com.creamaker.changli_planet_app.feature.timetable.viewmodel.TimeTableViewModel
import com.creamaker.changli_planet_app.utils.Event.SelectEvent
import com.creamaker.changli_planet_app.utils.EventBusLifecycleObserver
import com.creamaker.changli_planet_app.widget.view.DividerItemDecoration
import com.creamaker.changli_planet_app.widget.view.MaxHeightLinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.greenrobot.eventbus.Subscribe


class TimetableWheelBottomDialog(
    val mcontext: Context,
    val stuNum: String,
    val stuPassword: String,
    val vm: TimeTableViewModel,
    val maxHeight: Int,
    val refresh:()->Unit
) : BottomSheetDialogFragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TimeTableSelectorAdapter
    private lateinit var text: String
    private lateinit var item: List<String>
    private var selectedIndex = 0
    private var onInvoke: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        lifecycle.addObserver(EventBusLifecycleObserver(this))
        val view = inflater.inflate(R.layout.select_in_timetable, container, false)
        recyclerView = view.findViewById(R.id.selectRecyclerTimetable)

        val maxHeightLinearLayout = view.findViewById<MaxHeightLinearLayout>(R.id.maxHeightLayout)
        maxHeightLinearLayout.setMaxHeight(maxHeight)
        adapter = TimeTableSelectorAdapter(mcontext, stuNum, stuPassword, item, vm,refresh)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.scrollToPosition(selectedIndex)
        recyclerView.addItemDecoration(DividerItemDecoration())
        return view
    }

    fun setItem(list: List<String>) {
        item = list
    }

    fun setTitle(title: String) {
        text = title
    }

    @Subscribe
    fun ClickEvent(selectEvent: SelectEvent) {
        if (selectEvent.eventType == 1) {
            dismiss()
        }
    }
}