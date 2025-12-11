package com.dcelysia.csust_spider.education.data.remote.model

import java.time.LocalDateTime

data class ExamArrange(
    val campus: String,

    val session: String,
    /// 课程编号
    val courseIDval: String,
    /// 课程名称
    val courseNameval: String,
    /// 授课教师
    val teacherval: String,
    /// 考试时间
    val examTime: String,
    /// 考试开始时间
    val examStartTimeval: LocalDateTime?,
    /// 考试结束时间
    val examEndTimeval: LocalDateTime?,
    /// 考场
    val examRoomval: String,
    /// 座位号
    val seatNumberval: String,
    /// 准考证号
    val admissionTicketNumberval: String,
    /// 备注
    val remarksval: String,

    )