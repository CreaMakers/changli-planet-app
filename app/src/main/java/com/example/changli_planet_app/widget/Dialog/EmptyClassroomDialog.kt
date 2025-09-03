package com.example.changli_planet_app.widget.Dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.R
import com.example.changli_planet_app.feature.common.ui.adapter.EmptyClassroomAdapter
import com.example.changli_planet_app.widget.View.DividerItemDecoration

class EmptyClassroomDialog(
    context: Context,
    val list: List<String>,
) :
    Dialog(context) {
    companion object{
        private var currentDialog:EmptyClassroomDialog?=null

        fun showDialog(context: Context,list: List<String>){
            if(currentDialog==null){                                  //只有当前页面没有Dialog时才创造实例，防止多个实例
                currentDialog= EmptyClassroomDialog(context,list)
                currentDialog?.show()
            }
        }
    }
    private lateinit var yes: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EmptyClassroomAdapter
    private var selectedIndex = 0

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setWindowAnimations(R.style.DialogAnimation)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.empty_classroom_dialog)
        recyclerView = findViewById(R.id.empty_classroom_recycler)
        adapter = EmptyClassroomAdapter(list)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration())
        recyclerView.scrollToPosition(selectedIndex)

        yes = findViewById(R.id.chosen_yes)
        yes.setOnClickListener {
            dismiss()
        }
    }

    override fun dismiss() {
        super.dismiss()
        currentDialog=null
    }
}