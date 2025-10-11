package com.creamaker.changli_planet_app.widget.View

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.creamaker.changli_planet_app.R
import com.github.gzuliyujiang.wheelview.contract.OnWheelChangedListener
import com.github.gzuliyujiang.wheelview.widget.WheelView
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Calendar

class DatePickerDialog(context: Context) : BottomSheetDialog(context) {
    private var yearWheel: WheelView
    private var monthWheel: WheelView
    private var dayWheel: WheelView
    private var onDateSelectedListener: ((year: Int, month: Int, day: Int) -> Unit)? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_data_picker, null)
        setContentView(view)

        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.attributes?.windowAnimations = R.style.BottomSheetAnimation

        yearWheel = view.findViewById(R.id.yearWheel)
        monthWheel = view.findViewById(R.id.monthWheel)
        dayWheel = view.findViewById(R.id.dayWheel)

        // 设置数据范围
        val years = (1900..2100).map { it.toString() }
        val months = (1..12).map { String.format("%02d", it) }
        yearWheel.setData(years)
        monthWheel.setData(months)
        updateDays()

        // 监听年月变化，更新天数
        yearWheel.setOnWheelChangedListener(object : OnWheelChangedListener {
            override fun onWheelScrolled(view: WheelView?, offset: Int) {}
            override fun onWheelSelected(view: WheelView?, position: Int) {
                updateDays()
            }
            override fun onWheelScrollStateChanged(view: WheelView?, state: Int) {}
            override fun onWheelLoopFinished(view: WheelView?) {}
        })

        monthWheel.setOnWheelChangedListener(object : OnWheelChangedListener {
            override fun onWheelScrolled(view: WheelView?, offset: Int) {}
            override fun onWheelSelected(view: WheelView?, position: Int) {
                updateDays()
            }
            override fun onWheelScrollStateChanged(view: WheelView?, state: Int) {}
            override fun onWheelLoopFinished(view: WheelView?) {}
        })

        view.findViewById<TextView>(R.id.dialog_data_cancel).setOnClickListener { dismiss() }
        view.findViewById<TextView>(R.id.dialog_data_confirm).setOnClickListener {
            val year = yearWheel.getCurrentPosition() + 1900
            val month = monthWheel.getCurrentPosition() + 1
            val day = dayWheel.getCurrentPosition() + 1
            onDateSelectedListener?.invoke(year, month, day)
            dismiss()
        }
    }

    private fun updateDays() {
        val year = yearWheel.getCurrentPosition() + 1900
        val month = monthWheel.getCurrentPosition() + 1
        val days = getDaysInMonth(year, month)
        dayWheel.setData((1..days).map { String.format("%02d", it) })
    }

    private fun getDaysInMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun setDate(year: Int, month: Int, day: Int) {
        yearWheel.setDefaultPosition(year - 1900)
        monthWheel.setDefaultPosition(month - 1)
        dayWheel.setDefaultPosition(day - 1)
    }

    fun setOnDateSelectedListener(listener: (year: Int, month: Int, day: Int) -> Unit) {
        onDateSelectedListener = listener
    }
}