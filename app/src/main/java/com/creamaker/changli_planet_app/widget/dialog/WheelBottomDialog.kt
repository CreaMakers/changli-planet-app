package com.creamaker.changli_planet_app.widget.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.feature.common.ui.adapter.SelectorAdapter
import com.creamaker.changli_planet_app.widget.view.DividerItemDecoration
import com.creamaker.changli_planet_app.widget.view.MaxHeightLinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WheelBottomDialog(
    val maxHeight: Int,
    val onItemSelect: (String) -> Unit
) : BottomSheetDialogFragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SelectorAdapter
    private lateinit var item: List<String>
    private var selectedIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.select_dor_school, container, false)

        val maxHeightLinearLayout = view.findViewById<MaxHeightLinearLayout>(R.id.maxHeightLayout)
        maxHeightLinearLayout.setMaxHeight(maxHeight)

        recyclerView = view.findViewById(R.id.selector)
        // Pass a wrapper to adapter that calls the external callback AND dismisses the dialog
        adapter = SelectorAdapter(item) { selectedValue ->
            onItemSelect(selectedValue)
            dismiss()
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.scrollToPosition(selectedIndex)
        recyclerView.addItemDecoration(DividerItemDecoration())
        return view
    }

    fun setItem(list: List<String>) {
        item = list.toList()
    }

    // Unused but keeping for compatibility if needed, though setTitle was not used in previous code view except assignment
    fun setTitle(title: String) {
        // text = title // text was unused in previous file
    }
}