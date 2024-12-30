package com.example.changli_planet_app.Cache.Room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single


@Dao
interface CourseDao {
    @Query("DELETE FROM courses")
    fun clearAllCourses()

    //删除自定义课程
    @Query("DELETE FROM courses WHERE start = :start AND weekday = :day AND :curDisplayWeek IN (weeks) AND isCustom = 1")
    fun deleteCourse(start: Int, day: Int, curDisplayWeek: Int): Single<Int>

    // 获取所有课程
    @Query("SELECT * FROM courses")
    fun getAllCourses(): Single<MutableList<MySubject>>

    // 插入单个课程
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertCourse(subject: MySubject): Long

    // 批量插入课程
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertCourses(subjects: MutableList<MySubject>): List<Long>

}
