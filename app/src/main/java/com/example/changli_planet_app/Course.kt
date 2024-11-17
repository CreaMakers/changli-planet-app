package com.example.changli_planet_app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Course(
    val courseName: String,
    val teacher: String,
//    val weeks: List<Int>,
    val classroom: String,
    val weekday: String,
    val start: Int,
    val step: Int
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
