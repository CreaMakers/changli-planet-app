package com.creamaker.changli_planet_app.widget.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.common.redux.store.UserStore
import com.creamaker.changli_planet_app.settings.ui.adapter.UserProfileSelectorAdapter
import com.creamaker.changli_planet_app.utils.Event.SelectEvent
import com.creamaker.changli_planet_app.utils.EventBusLifecycleObserver
import com.creamaker.changli_planet_app.widget.view.DividerItemDecoration
import com.creamaker.changli_planet_app.widget.view.MaxHeightLinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.greenrobot.eventbus.Subscribe


class UserProfileWheelBottomDialog(
    val mcontext: Context,
    val store: UserStore,
    val maxHeight: Int,
    val changeGender: (String) -> Unit,
    val changeGrade: (String) -> Unit
) : BottomSheetDialogFragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserProfileSelectorAdapter
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

        adapter = UserProfileSelectorAdapter(mcontext, item, store, changeGender, changeGrade)
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