package com.example.changli_planet_app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.changli_planet_app.Network.Response.Course

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.Flowable

@Dao
interface CourseDao {

    // 使用 Single 来获取所有课程列表，表示异步操作，且会返回一个结果
    @Query("SELECT * FROM courses")
    fun getAllCourse(): Single<MutableList<MySubject>>  // Single 是 RxJava 中表示一个单一结果的类型

    // 插入课程信息，返回一个 Completable，表示操作完成
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCourse(subject: MySubject): Completable  // Completable 表示无返回值，只表示完成状态

    // 更新课程信息，返回一个 Completable，表示操作完成
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateCourse(subjects: MutableList<MySubject>): Completable
}
