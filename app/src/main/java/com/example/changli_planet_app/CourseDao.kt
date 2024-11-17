package com.example.changli_planet_app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CourseDao {
    @Query("select * from Course")
    fun getAllCourse() : List<Course>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCourse(course: Course)


}