package com.example.changli_planet_app.Cache.Room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single


@Dao
interface CourseDao {

    // 获取所有课程
    @Query("SELECT * FROM courses")
    fun getAllCourses(): Single<MutableList<MySubject>>

    // 插入单个课程
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCourse(subject: MySubject): Completable

    // 批量插入课程
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCourses(subjects: MutableList<MySubject>): Completable

    // 更新多个课程
    @Update
    fun updateCourses(subjects: MutableList<MySubject>): Completable
}
