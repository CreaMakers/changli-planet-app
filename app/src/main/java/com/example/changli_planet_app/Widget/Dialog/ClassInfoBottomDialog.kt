package com.example.changli_planet_app.Widget.Dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Feature.common.ui.adapter.ClassInfoAdapter
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Utils.Event.SelectEvent
import com.example.changli_planet_app.Utils.EventBusLifecycleObserver
import com.example.changli_planet_app.Widget.View.DividerItemDecoration
import com.example.changli_planet_app.Widget.View.MaxHeightLinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.greenrobot.eventbus.Subscribe


class ClassInfoBottomDialog(
    val maxHeight: Int,
    val changeWeek: (String) -> Unit,
    val changeDay: (String) -> Unit,
    val changeRegion: (String) -> Unit
) : BottomSheetDialogFragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClassInfoAdapter
    private lateinit var text: String
    private lateinit var item: List<String>
    private var selectedIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        lifecycle.addObserver(EventBusLifecycleObserver(this))
        val view = inflater.inflate(R.layout.select_in_userprofile, container, false)
        recyclerView = view.findViewById(R.id.selectRecyclerUserProfile)

        val maxHeightLinearLayout = view.findViewById<MaxHeightLinearLayout>(R.id.maxHeightLayout)
        maxHeightLinearLayout.setMaxHeight(maxHeight)

        adapter = ClassInfoAdapter(item, changeWeek, changeDay, changeRegion)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration())
        recyclerView.scrollToPosition(selectedIndex)
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