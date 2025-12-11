package com.dcelysia.csust_spider.education.data.remote.model

import com.dcelysia.csust_spider.education.data.remote.model.CourseNature

data class CourseGrade(
    val semester: String, //开课学期
    val courseID: String, //课程编号
    val courseName: String, //课程名称
    val groupName: String, //分组名
    val grade: Int, // 成绩
    val gradeDetailUrl: String, //详细成绩链接
    val studyMode: String, //修读方式
    val gradeIdentifier: String, //成绩标识
    val credit: Double, //学分
    val totalHours: Int, //总学时
    val gradePoint: Double, //绩点
    val retakeSemester: String, //补重学期
    val assessmentMethod: String, //考核方式
    val examNature: String, //考试性质
    val courseAttribute: String, //课程属性
    val courseNature: CourseNature, //课程性质
    val courseCategory: String //课程类别
)

data class CourseGradeResponse(
    val code: String,
    val msg: String,
    val data: List<CourseGrade>? = null
)