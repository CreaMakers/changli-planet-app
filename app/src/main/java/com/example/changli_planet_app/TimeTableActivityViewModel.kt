package com.example.changli_planet_app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.changli_planet_app.MySubject
import com.example.changli_planet_app.SubjectRepertory

object TimeTableActivityViewModel : ViewModel() {
//  val subjects : MutableList<MySubject>
//      get() {
//
//          val list = mutableListOf<MySubject>()
//          list.addAll(SubjectRepertory.loadDefaultSubjects())
//          list.addAll(SubjectRepertory.loadDefaultSubjects2())
//          return list
//      }
// subjects 只初始化一次
val subjects: MutableList<MySubject> by lazy {
    mutableListOf<MySubject>().apply {
        addAll(SubjectRepertory.loadDefaultSubjects())
        addAll(SubjectRepertory.loadDefaultSubjects2())
    }
}
fun addSubject(subject: MySubject) {
    subjects.add(subject)
}
// 使用 MutableLiveData 来持有 subjects 列表
//private val _subjects = MutableLiveData<MutableList<MySubject>>().apply {
//    value = mutableListOf<MySubject>().apply {
//        addAll(SubjectRepertory.loadDefaultSubjects())
//        addAll(SubjectRepertory.loadDefaultSubjects2())
//    }
//}
//    val subjects: LiveData<MutableList<MySubject>> get() = _subjects
//    fun addSubject(subject: MySubject) {
//        _subjects.value?.let {
//            it.add(subject)
//            _subjects.value = it // 触发 LiveData 更新
//        }
//    }

}