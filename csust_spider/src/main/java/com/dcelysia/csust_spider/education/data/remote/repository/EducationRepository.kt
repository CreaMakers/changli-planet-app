package com.dcelysia.csust_spider.education.data.remote.repository

import android.util.Log
import android.util.Log.e
import com.dcelysia.csust_spider.core.Resource
import com.dcelysia.csust_spider.core.RetrofitUtils
import com.dcelysia.csust_spider.education.data.remote.api.CourseGradeApi
import com.dcelysia.csust_spider.education.data.remote.api.CourseScheduleApi
import com.dcelysia.csust_spider.education.data.remote.error.EduHelperError
import com.dcelysia.csust_spider.education.data.remote.model.Course
import com.dcelysia.csust_spider.education.data.remote.model.CourseGrade
import com.dcelysia.csust_spider.education.data.remote.model.CourseGradeResponse
import com.dcelysia.csust_spider.education.data.remote.model.CourseNature
import com.dcelysia.csust_spider.education.data.remote.model.DisplayMode
import com.dcelysia.csust_spider.education.data.remote.model.GradeComponent
import com.dcelysia.csust_spider.education.data.remote.model.GradeDetail
import com.dcelysia.csust_spider.education.data.remote.model.GradeDetailResponse
import com.dcelysia.csust_spider.education.data.remote.model.StudyMode
import com.dcelysia.csust_spider.education.data.remote.services.AuthService
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.lang.Exception
import kotlin.jvm.java

class EducationRepository private constructor() {
    private val TAG = "EducationRepository"
    companion object {
        val instance by lazy { EducationRepository() }
        val TAG = "EducationRepository"
    }

    private val courseScheduleApi by lazy { RetrofitUtils.instanceEduCourse.create(CourseScheduleApi::class.java) }
    private val courseGradeApi by lazy { RetrofitUtils.instanceScoreInquiry.create(CourseGradeApi::class.java) }
//    private val relexClassroomInfoApi by lazy {
//        RetrofitUtils
//            .instanceRelexClassroomInfo.create(
//                RelexClassroomInfoApi::class.java
//            )
//    }

    /**
     * Gets course schedule by term and parses it into a List of Course objects
     * 
     * @param week The week number
     * @param academicSemester The academic semester identifier
     * @return List of Course objects parsed from the HTML response
     */
    suspend fun getCourseScheduleByTerm(week: String, academicSemester: String): Resource<List<Course>> {
        return try {
            val response = courseScheduleApi.getCourseSchedule(week, academicSemester)
            if (!response.isSuccessful){
                return Resource.Error("网络请求失败")
            }
            val htmlBody = response.body().toString()
            Log.d(Companion.TAG,htmlBody)
            if (response.code() == 200 && htmlBody.contains("用户登录")){
                return Resource.Error("需要重新登录")
            }

            parseCourseSchedule(htmlBody)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching course schedule", e)
            Resource.Error("发生错误")
        }
    }
    
    /**
     * Parses HTML response into a List of Course objects
     * 
     * @param html The HTML response string from the course schedule API
     * @return List of Course objects parsed from the HTML
     */
    fun parseCourseSchedule(html: String): Resource<List<Course>> {
        val courses = mutableListOf<Course>()
        
        try {
            val document: Document = Jsoup.parse(html)
            
            // 检查是否有"未查询到数据"的提示
            val emptyDataElement = document.selectFirst("td[colspan=10]")
            if (emptyDataElement != null && emptyDataElement.text().contains("未查询到数据")) {
                Log.d(TAG, "未查询到课程数据")
                return Resource.Error("未查询到课程")
            }
            
            // 查找所有课程内容div
            val courseDivs = document.select("div.kbcontent")
            
            for (div in courseDivs) {
                // 检查是否有分隔符 "---------------------"
                val courseBlocks = div.html().split("---------------------")
                
                for (block in courseBlocks) {
                    // 为每个课程块创建新的Element
                    val courseBlock = Jsoup.parse(block).body()
                    
                    // 提取课程名称 - 使用第一行文本
                    val courseName = courseBlock.ownText().trim()
                    
                    var teacher: String? = null
                    var weeks: String? = null
                    var classroom: String? = null
                    var weekday: String? = null
                    
                    // 获取原始div的id并解析出星期
                    val divId = div.attr("id")
                    if (divId != null && divId.isNotEmpty()) {
                        val idParts = divId.split("-")
                        if (idParts.size >= 2) {
                            weekday = idParts[idParts.size - 2]
                        }
                    }
                    
                    // 查找当前课程块的信息
                    val fonts = courseBlock.select("font")
                    for (font in fonts) {
                        val title = font.attr("title")
                        val text = font.text()
                        
                        when (title) {
                            "老师" -> teacher = text
                            "周次(节次)" -> weeks = text
                            "教室" -> classroom = text
                        }
                    }
                    
                    // 只有当课程名不为空时才添加到列表中
                    if (courseName.isNotEmpty()) {
                        courses.add(
                            Course(
                                courseName = courseName,
                                teacher = teacher ?: "",
                                weeks = weeks ?: "",
                                classroom = classroom ?: "",
                                weekday = weekday ?: ""
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing course schedule HTML", e)
        }
        
        return Resource.Success(courses)
    }

    /* 获取课程成绩
    * - Parameters:
    *   - academicYearSemester: 学年学期，格式为 "2023-2024-1"，如果为 `nil` 则为全部学期
    *   - courseNature: 课程性质，如果为 `nil` 则查询所有性质的课程
    *   - courseName: 课程名称，默认为空字符串表示查询所有课程
    *   - displayMode: 显示模式，默认为显示最好成绩
    *   - studyMode: 修读方式，默认为主修
    * - Throws: `EduHelperError`
    * Returns: 课程成绩信息数组
    */
    suspend fun getCourseGrades(
        academicYearSemester: String? = null,
        courseNature: CourseNature? = null,
        courseName: String = "",
        displayMode: DisplayMode = DisplayMode.BEST_GRADE,
        studyMode: StudyMode = StudyMode.MAJOR
    ): CourseGradeResponse {
        val response = courseGradeApi.getCourseGrades(
            academicYearSemester ?: "", //学年学期，格式为 "2023-2024-1"，如果为 `nil` 则为全部学期
            courseNature?.id ?: "", //课程性质，如果为 `nil` 则查询所有性质的课程
            courseName, //课程名称，默认为空字符串表示查询所有课程
            displayMode.id, //显示模式，默认为显示最好成绩
            studyMode.id //修读方式，默认为主修
        )

        if (!response.isSuccessful) {
            return CourseGradeResponse(
                response.code().toString(),
                "网络请求失败：${response.message()}",
                null
            )
        }

        val html = response.body()
        if (html.isNullOrBlank()) {
            return CourseGradeResponse(
                code = "-1",
                msg = "响应体为空",
                data = null
            )
        }

        if (html.contains("用户登录") || html.contains("统一身份认证")) {
            return CourseGradeResponse("403", "登录状态已失效，请重新登录", null)
        }

        return try {
            val grades = parseCourseGrades(html)
            CourseGradeResponse("200", "成功", grades)
        } catch (e: Exception) {
            CourseGradeResponse("-2", "解析失败: ${e.message}", null)
        }
    }

    private fun parseCourseGrades(html: String): List<CourseGrade> {

        // 可以加withContext(Dispatchers.IO)包裹耗时的Jsoup解析-属于CPU密集型
        val document = Jsoup.parse(html)

        // 查找成绩表格
        val table = document.selectFirst("#dataList")
            ?: throw EduHelperError.CourseGradesRetrievalFailed("未找到课程成绩表格")

        if (table.text().contains("未查询到数据")) return emptyList()

        val rows = table.select("tr")
        val courseGrades = mutableListOf<CourseGrade>()

        for (i in 1 until rows.size) {
            val row = rows[i]
            val cols = row.select("td")

            if (cols.size < 17) {
                throw EduHelperError.CourseGradesRetrievalFailed("行列数不足：${cols.size}")
            }

            try{
                // 按列依次解析每个字段 去掉多余空格
                val semester = cols[1].text().trim()
                val courseID = cols[2].text().trim()
                val courseName = cols[3].text().trim()
                val groupName = cols[4].text().trim()
                val gradeString = cols[5].text().trim()
                val grade = gradeString.toIntOrNull()
                    ?: throw EduHelperError.CourseGradesRetrievalFailed("成绩格式无效：$gradeString")

                // 解析成绩详情链接
                val gradeDetailUrlElement = cols[5].select("a")
                var gradeDetailUrl = gradeDetailUrlElement?.attr("href")?.trim() ?: ""
                gradeDetailUrl = gradeDetailUrl
                    .replace("javascript:openWindow('","http://xk.csust.edu.cn")
                    .replace("',700,500)","")

                val studyMode = cols[6].text().trim()
                val gradeIdentifier = cols[7].text().trim()
                val credit = cols[8].text().trim().toDoubleOrNull()
                    ?: throw EduHelperError.CourseGradesRetrievalFailed("学分格式无效: ${cols[8].text()}")

                val totalHours = cols[9].text().trim().toIntOrNull()
                    ?: throw EduHelperError.CourseGradesRetrievalFailed("总学时格式无效: ${cols[9].text()}")

                val gradePoint = cols[10].text().trim().toDoubleOrNull()
                    ?: throw EduHelperError.CourseGradesRetrievalFailed("绩点格式无效: ${cols[10].text()}")

                val retakeSemester = cols[11].text().trim()
                val assessmentMethod = cols[12].text().trim()
                val examNature = cols[13].text().trim()
                val courseAttribute = cols[14].text().trim()

                val courseNatureString = cols[15].text().trim()
                val courseNature = CourseNature.Companion.fromChineseName(courseNatureString)

                val courseCategory = cols[16].text().trim()

                val courseGrade = CourseGrade(
                    semester, courseID, courseName, groupName, grade, gradeDetailUrl,
                    studyMode, gradeIdentifier, credit, totalHours, gradePoint, retakeSemester,
                    assessmentMethod, examNature, courseAttribute, courseNature, courseCategory
                )

                courseGrades.add(courseGrade)
            } catch (e: kotlin.Exception) {
                if (e is EduHelperError) throw e
                throw EduHelperError.CourseGradesRetrievalFailed("解析成绩时出错：${e.message}")
            }
        }

        return courseGrades
    }

    /* 获取课程成绩的所有可用学期
    * - Throws: `EduHelperError`
    * - Returns: 包含所有可用学期的数组
    */
    suspend fun getAvailableSemestersForCourseGrades(): List<String> {
        val response = courseGradeApi.getCourseGradePage()

        if(!response.isSuccessful) {
            throw EduHelperError.AvailableSemestersForCourseGradesRetrievalFailed("网络请求失败：${response.code()}")
        }

        val html = response.body() ?: throw EduHelperError.AvailableSemestersForCourseGradesRetrievalFailed("响应体为空")
        return parseAvailableSemesters(html)
    }

    private fun parseAvailableSemesters(html: String): List<String> {
        val document = Jsoup.parse(html)
        val semesterSelect = document.selectFirst("#kksj")
            ?: throw EduHelperError.AvailableSemestersForCourseGradesRetrievalFailed("未找到学期选择元素")

        val options = semesterSelect.select("option")
        val semesters = mutableListOf<String>()

        for (option in options) {
            val name = option.text().trim()
            if (name.contains("全部学期")) continue
            semesters.add(name)
        }

        // 返回学期列表，如 ["2024-2025-1", "2024-2025-2", ...]
        return semesters
    }

    /* 获取成绩详情
    * - Parameter url: 课程详细URL
    * - Throws: `EduHelperError`
    * - Returns: 成绩详情
    */
    suspend fun getGradeDetail(url: String): GradeDetailResponse {
        val response = courseGradeApi.getGradeDetail(url)

        if (!response.isSuccessful) {
            return GradeDetailResponse(
                response.code().toString(),
                "网络请求失败：${response.message()}",
                null
            )
        }

        val html = response.body()
        if (html.isNullOrBlank()) {
            return GradeDetailResponse(
                code = "-1",
                msg = "响应体为空",
                data = null
            )
        }

        if (html.contains("用户登录") || html.contains("统一身份认证")) {
            return GradeDetailResponse("403", "登录状态已失效，请重新登录", null)
        }

        return try {
            val gradeDetail = parseGradeDetail(html)
            GradeDetailResponse("200","成功",gradeDetail)
        } catch (e: Exception) {
            GradeDetailResponse("-2","解析失败：${e.message}",null)
        }
    }

    private fun parseGradeDetail(html: String): GradeDetail {
        val document = Jsoup.parse(html)

        // 查找成绩详情表格
        val table = document.selectFirst("#dataList")
            ?: throw EduHelperError.GradeDetailRetrievalFailed("未找到成绩详情表格")

        val rows = table.select("tr")
        if (rows.size < 2) {
            throw EduHelperError.GradeDetailRetrievalFailed("成绩详情表行数不足")
        }

        // 表头-存放成绩组成的名称
        val headerRow = rows[0]
        val headerCols = headerRow.select("th")

        // 数据行-存放对应的分数和比例
        val valueRow = rows[1]
        val valueCols = valueRow.select("td")

        // 至少要有“组成名称/分数/比例/总成绩”四列
        if (headerCols.size < 4 || valueCols.size < 4) {
            throw EduHelperError.GradeDetailRetrievalFailed("成绩详情表列数不足")
        }

        val components = mutableListOf<GradeComponent>()

        for (i in 1 until headerCols.size - 1 step 2) {
            val type = headerCols[i].text().trim()
            val gradeString = valueCols[i].text().trim()
            val ratioString = valueCols[i + 1].text().trim().replace("%", "")

            val grade = gradeString.toDoubleOrNull()
                ?: throw EduHelperError.GradeDetailRetrievalFailed("成绩格式无效: $gradeString")

            val ratio = ratioString.toIntOrNull()
                ?: throw EduHelperError.GradeDetailRetrievalFailed("比例格式无效: $ratioString")

            components.add(GradeComponent(type, grade, ratio))
        }

        val totalGradeString = valueCols.last()?.text()?.trim()
        val totalGrade = totalGradeString?.toIntOrNull()
            ?: throw EduHelperError.GradeDetailRetrievalFailed("总成绩格式无效: $totalGradeString")

        return GradeDetail(components, totalGrade)
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
//            val response = relexClassroomInfoApi.getRelexClassroom(
//                xnxqh = xnxqh,
//                xqbh = xqbh,
//                zc = zc,
//                zc2 = zc2,
//                xq = xq,
//                xq2 = xq2,
//                jc = jc,
//                jc2 = jc2
//            )
//
//            if (!response.isSuccessful) {
//                return Resource.Error("网络请求失败")
//            }
//
//            val htmlBody = response.body().toString()
//            Log.d(Companion.TAG, htmlBody)
//            if (response.code() == 200 && htmlBody.contains("用户登录")) {
//                return Resource.Error("需要重新登录")
//            }
//
//            Resource.Success(htmlBody)
//        } catch (e: Exception) {
//            Log.e(TAG, "Error fetching classroom information", e)
//            Resource.Error("发生错误")
//        }
//    }
}