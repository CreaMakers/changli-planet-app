package com.example.changli_planet_app

import android.content.Context
import android.util.AttributeSet
import com.zhuangfei.timetable.TimetableView

class NewTimeTableView(context: Context,attrs: AttributeSet) : TimetableView(context,attrs) {

    override fun curWeek(startTime: String?): TimetableView {
        return super.curWeek(startTime)
    }

//    override fun curWeek(curWeek: Int): TimetableView {
//        if (curWeek < 1) super.curWeek = 1
//        else if (curWeek > 25) this.curWeek = 25
//        else this.curWeek = curWeek
//        onBind(curWeek)
//        return this
//    }
//
}