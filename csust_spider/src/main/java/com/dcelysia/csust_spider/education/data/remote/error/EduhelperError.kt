package com.dcelysia.csust_spider.education.data.remote.error

sealed class EduHelperError(message: String) : Exception(message) {
    class LoginFailed(message: String) : EduHelperError(message)
    class NotLoggedIn(message: String) : EduHelperError(message)
    // 课程成绩获取失败
    class CourseGradesRetrievalFailed(message: String) : EduHelperError(message)
    // 课程成绩获取失败
    class GradeDetailRetrievalFailed(message: String) : EduHelperError(message)
    // 成绩详情获取失败
    class AvailableSemestersForCourseGradesRetrievalFailed(message: String) : EduHelperError(message)
    class  examScheduleRetrievalFailed(message: String) : EduHelperError(message)
    /// 考试安排可选学期获取失败
    class availableSemestersForExamScheduleRetrievalFailed(message: String): EduHelperError(message)

    class TimeParseFailed(message: String): EduHelperError(message)
}
