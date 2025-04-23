package com.example.changli_planet_app.Cache.Room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.changli_planet_app.Cache.Room.entity.MySubject
import io.reactivex.rxjava3.core.Single

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun getAllCourses(): Single<MutableList<MySubject>>

    @Query("SELECT * FROM courses WHERE term = :term AND studentId = :studentId AND studentPassword = :studentPassword")
    fun getCoursesByTerm(
        term: String,
        studentId: String,
        studentPassword: String
    ): Single<MutableList<MySubject>>

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    fun insertCourse(subject: MySubject): Long

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    fun insertCourses(subjects: MutableList<MySubject>): List<Long>

    @Query("DELETE FROM courses")
    fun clearAllCourses()

    @Query("DELETE FROM courses WHERE start = :start AND weekday = :day AND :curDisplayWeek IN (weeks) AND isCustom = 1")
    fun deleteCourse(start: Int, day: Int, curDisplayWeek: Int): Single<Int>

    @Query("SELECT COUNT(*) FROM courses")
    fun getAllCourseCount(): Single<Int>

}