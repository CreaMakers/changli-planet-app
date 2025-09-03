package com.example.changli_planet_app.widget.Dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.R
import com.example.changli_planet_app.feature.common.redux.store.ElectronicStore
import com.example.changli_planet_app.feature.common.ui.adapter.SelectorAdapter
import com.example.changli_planet_app.utils.Event.SelectEvent
import com.example.changli_planet_app.utils.EventBusLifecycleObserver
import com.example.changli_planet_app.widget.View.DividerItemDecoration
import com.example.changli_planet_app.widget.View.MaxHeightLinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.greenrobot.eventbus.Subscribe

class WheelBottomDialog(
    val store: ElectronicStore, val maxHeight: Int
) :
    BottomSheetDialogFragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SelectorAdapter
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
        val view = inflater.inflate(R.layout.select_dor_school, container, false)

        val maxHeightLinearLayout = view.findViewById<MaxHeightLinearLayout>(R.id.maxHeightLayout)
        maxHeightLinearLayout.setMaxHeight(maxHeight)

        recyclerView = view.findViewById(R.id.selector)
        adapter = SelectorAdapter(item, store)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.scrollToPosition(selectedIndex)
        recyclerView.addItemDecoration(DividerItemDecoration())
        return view
    }

    fun setItem(list: List<String>) {
        item = list.toList()
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