//package com.creamaker.changli_planet_app.feature.common.redux.action
//
//import android.content.Context
//
///**
// * 空闲教室Action
// */
//sealed class ClassInfoAction {
//    object initilaize : ClassInfoAction()
//    class UpdateWeek(val week: String) : ClassInfoAction()
//    class UpdateDay(val day: String) : ClassInfoAction()
//    class UpdateRegion(val region: String) : ClassInfoAction()
//    class UpdateStartAndEnd(val start: String, val end: String) : ClassInfoAction()
//
//    class QueryEmptyClassInfo(val context: Context, val term: String) : ClassInfoAction()
//}