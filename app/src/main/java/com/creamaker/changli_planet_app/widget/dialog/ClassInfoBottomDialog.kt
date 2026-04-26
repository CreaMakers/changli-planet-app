package com.creamaker.changli_planet_app.widget.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.feature.common.ui.adapter.ClassInfoAdapter
import com.creamaker.changli_planet_app.utils.event.AppEventBus
import com.creamaker.changli_planet_app.widget.view.DividerItemDecoration
import com.creamaker.changli_planet_app.widget.view.MaxHeightLinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch


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
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                AppEventBus.selectEvent.collect { event ->
                    if (event.eventType == 1) dismiss()
                }
            }
        }
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
}