package com.example.changli_planet_app.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Adapter.SelectorAdapter
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Util.Event.SelectEvent
import com.example.changli_planet_app.Util.EventBusHelper
import com.example.changli_planet_app.Util.EventBusLifecycleObserver
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.greenrobot.eventbus.Subscribe

class WheelBottomDialog : BottomSheetDialogFragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter : SelectorAdapter
    private lateinit var text:String
    private lateinit var item : List<String>
    private var selectedIndex = 0
    private var onInvoke : ((String)->Unit) ?= null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        lifecycle.addObserver(EventBusLifecycleObserver(this))
        val view = inflater.inflate(R.layout.select_dor_school,container,false)
        recyclerView = view.findViewById(R.id.selector)
        adapter = SelectorAdapter(item)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.scrollToPosition(selectedIndex)
        return view
    }
    fun setItem(list: List<String>){
        item = list
    }
    fun setTitle(title:String){
        text = title
    }
    @Subscribe
    fun ClickEvent(selectEvent: SelectEvent){
        if(selectEvent.eventType==1){
            EventBusHelper.post(SelectEvent(selectEvent.text,2))
            dismiss()
        }
    }
}