package com.creamaker.changli_planet_app.feature.common.data.local.entity

import com.dcelysia.csust_spider.education.data.remote.model.ExamArrange
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

//data class ExamArrangement (
//    val id: String,
//    val place: String,
//    val examId: String,
//    val CourseId: String,
//    val name: String,
//    val teacher: String,
//    val time: String,
//    val room: String,
//    @SerializedName("courseId")
//    val courseId2: String,
//)

//新数据格式，但具体结构采用网络库里头的
//data class ExamArrange(
//    val campus: String,
//
//    val session: String,
//    /// 课程编号
//    val courseIDval: String,
//    /// 课程名称
//    val courseNameval: String,
//    /// 授课教师
//    val teacherval: String,
//    /// 考试时间
//    val examTime: String,
//    /// 考试开始时间
//    val examStartTimeval: LocalDateTime?,
//    /// 考试结束时间
//    val examEndTimeval: LocalDateTime?,
//    /// 考场
//    val examRoomval: String,
//    /// 座位号
//    val seatNumberval: String,
//    /// 准考证号
//    val admissionTicketNumberval: String,
//    /// 备注
//    val remarksval: String,
//
//    )

data class ExamArrangementResponse (
    val code: String,
    val msg: String,
    val data: List<ExamArrange>
)