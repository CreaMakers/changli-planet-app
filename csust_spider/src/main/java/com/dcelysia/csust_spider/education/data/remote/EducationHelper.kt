package com.dcelysia.csust_spider.education.data.remote

import android.util.Log
import com.dcelysia.csust_spider.core.Resource
import com.dcelysia.csust_spider.education.data.remote.model.Course
import com.dcelysia.csust_spider.education.data.remote.model.CourseGradeResponse
import com.dcelysia.csust_spider.education.data.remote.model.CourseNature
import com.dcelysia.csust_spider.education.data.remote.model.DisplayMode
import com.dcelysia.csust_spider.education.data.remote.model.GradeDetailResponse
import com.dcelysia.csust_spider.education.data.remote.model.StudyMode
import com.dcelysia.csust_spider.education.data.remote.repository.EducationRepository

object EducationHelper {
    private val TAG = "EducationHelper"
    private val repository by lazy { EducationRepository.instance }
    
    /**
     * Gets course schedule by term and returns raw HTML string (backward compatibility)
     * 
     * @param week The week number
     * @param academicSemester The academic semester identifier
     * @return Raw HTML string of the course schedule
     */
    suspend fun getCourseScheduleByTerm(week: String, academicSemester: String): Resource<List<Course>> {
        return try {
            // For backward compatibility, we still call the API but return the raw response
            // In a real implementation, you might want to store the raw response separately
            getParsedCourseScheduleByTerm(week,academicSemester)
        } catch (e: Exception) {
            Log.d(TAG,e.toString())
            return Resource.Error("发生错误")
        }
    }
    
    /**
     * Gets course schedule by term and parses it into a List of Course objects
     * 
     * @param week The week number
     * @param academicSemester The academic semester identifier
     * @return List of Course objects parsed from the course schedule
     */
    suspend fun getParsedCourseScheduleByTerm(week: String, academicSemester: String): Resource<List<Course>> {
        return repository.getCourseScheduleByTerm(week, academicSemester)
    }

    /* 获取课程成绩
    * - Parameters:
    *   - academicYearSemester: 学年学期，格式为 "2023-2024-1"，如果为 `nil` 则为全部学期
    *   - courseNature: 课程性质，如果为 `nil` 则查询所有性质的课程
    *   - courseName: 课程名称，默认为空字符串表示查询所有课程
    *   - displayMode: 显示模式，默认为显示最好成绩
    *   - studyMode: 修读方式，默认为主修
    * Returns: 课程成绩信息数组
    */
    suspend fun getCourseGrades(
        academicYearSemester: String? = null,
        courseNature: CourseNature? = null,
        courseName: String = "",
        displayMode: DisplayMode = DisplayMode.BEST_GRADE,
        studyMode: StudyMode = StudyMode.MAJOR
    ): CourseGradeResponse? {
        return try {
            repository.getCourseGrades(academicYearSemester,courseNature,courseName,displayMode,studyMode)
        } catch (e: Exception) {
            Log.d(TAG,e.toString())
            null
        }
    }

    /* 获取课程成绩的所有可用学期
    * - Returns: 包含所有可用学期的数组
    */
    suspend fun getAvailableSemestersForCourseGrades(): List<String> {
        return try {
            repository.getAvailableSemestersForCourseGrades()
        } catch (e: Exception) {
            Log.d(TAG,e.toString())
            emptyList()
        }
    }

    /* 获取成绩详情
    * - Parameter url: 课程详细URL
    * - Returns: 成绩详情
    */
    suspend fun getGradeDetail(url: String): GradeDetailResponse? {
        return try {
            repository.getGradeDetail(url)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

//    /**
//     * Gets classroom information and returns raw HTML string
//     *
//     * @param xnxqh 学期字段，示例："2024-2025-1"
//     * @param xqbh 校区编号，用于指定查询的校区/区域
//     * @param zc 开始周次（查询的起始周）
//     * @param zc2 结束周次（查询的结束周）示例：第一周
//     * @param xq 开始星期，示例：星期一
//     * @param xq2 结束星期，示例：星期二
//     * @param jc 开始节次（节次的起始值）示例：01
//     * @param jc2 结束节次（节次的结束值）示例：02
//     * @return Raw HTML string of the classroom information
//     */
//    suspend fun getRelexClassroom(
//        xnxqh: String,
//        xqbh: String,
//        zc: String,
//        zc2: String,
//        xq: String,
//        xq2: String,
//        jc: String,
//        jc2: String
//    ): Resource<String> {
//        return try {
//            repository.getRelexClassroom(xnxqh, xqbh, zc, zc2, xq, xq2, jc, jc2)
//        } catch (e: Exception) {
//            Log.d(TAG, e.toString())
//            Resource.Error("发生错误")
//        }
//    }
}