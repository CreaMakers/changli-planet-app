package com.dcelysia.csust_spider.education.data.remote.services

import android.os.Build
import android.util.Log
import com.dcelysia.csust_spider.core.Resource
import com.dcelysia.csust_spider.core.RetrofitUtils
import com.dcelysia.csust_spider.education.data.remote.api.ExamApi
import com.dcelysia.csust_spider.education.data.remote.error.EduHelperError
import com.dcelysia.csust_spider.education.data.remote.model.ExamArrange
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.collections.get
import org.jsoup.Jsoup

// 确保你已经有了这个类，或者放在合适的文件中

object ExamArrangeService {

    private val api by lazy { RetrofitUtils.instanceExam.create(ExamApi::class.java) }
    private const val TAG = "Exam_Arrange"

    /** 获取考试安排 注意：Loading 状态建议在 ViewModel 调用此函数前设置 */
    suspend fun getExamArrange(
            semester: String,
            semesterType: String
    ): Resource<List<ExamArrange>> {
        return try {
            // 2. 获取学期信息 (如果在 try 块中抛出异常会被 catch 捕获)
            val querySemester =
                    semester.ifEmpty {
                        val semesters = getSemesterMessage()
                        if (semesters.isEmpty()) {
                            return Resource.Error("获取学期信息失败：列表为空")
                        }
                        semesters[0] // 默认取第一个，根据逻辑可能是默认选中的那个
                    }

            // 3. 网络请求
            val semesterId = getSemesterid(semesterType)
            val response = api.queryExamList(semesterType, querySemester, semesterId)

            if (!response.isSuccessful) {
                return Resource.Error("网络请求失败: code=${response.code()}")
            }

            val body = response.body()
            if (body.isNullOrEmpty()) {
                return Resource.Error("服务器返回数据为空")
            }

            // 4. 解析 HTML
            val html = Jsoup.parse(body)
            val dataDiv = html.select("#dataList").first()

            // 检查是否有数据表格
            if (dataDiv == null) {
                // 有时候教务系统报错也会返回 HTML，需要判断是否包含错误提示
                val errorMsg = html.select("font[color=red]").text()
                if (errorMsg.isNotEmpty()) {
                    return Resource.Error("教务系统提示: $errorMsg")
                }
                return Resource.Error("解析失败：未找到数据表格")
            }

            // 检查是否查无数据
            if (dataDiv.html().contains("未查询到数据")) {
                return Resource.Success(emptyList()) // 成功，但列表为空
            }

            val list = dataDiv.select("tr")
            val examList = mutableListOf<ExamArrange>()

            list.forEachIndexed { index, row ->
                if (index == 0) return@forEachIndexed // 跳过表头

                val cols = row.select("td")
                if (cols.size < 11) {
                    return Resource.Error("表格格式异常，列数不足: ${cols.size}")
                }

                // 解析时间
                val timeString = cols[6].text().trim()
                val examTimeRange =
                        try {
                            parseDate(timeString)
                        } catch (e: Exception) {
                            return Resource.Error("时间解析失败 ($timeString): ${e.message}")
                        }

                val exam =
                        ExamArrange(
                                cols[1].text().trim(),
                                cols[2].text().trim(),
                                cols[3].text().trim(),
                                cols[4].text().trim(),
                                cols[5].text().trim(),
                                cols[6].text().trim(),
                                examTimeRange.first,
                                examTimeRange.second,
                                cols[7].text().trim(),
                                cols[8].text().trim(),
                                cols[9].text().trim(),
                                cols[10].text().trim()
                        )
                examList.add(exam)
            }

            Resource.Success(examList)
        } catch (e: Exception) {
            Log.e(TAG, "getExamArrange error", e)
            // 捕获所有未知异常，防止 App 崩溃
            val errorMsg = if (e is EduHelperError) e.message else "未知错误: ${e.message}"
            Resource.Error(errorMsg ?: "发生未知错误")
        }
    }

    // 移除了 suspend，纯逻辑处理
    private fun parseDate(timeString: String): Pair<LocalDateTime, LocalDateTime> {
        val list = timeString.split(" ")
        if (list.size != 2) throw EduHelperError.TimeParseFailed("日期格式错误")

        val timeList = list[1].split("~")
        if (timeList.size != 2) throw EduHelperError.TimeParseFailed("时间段格式错误")

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            throw EduHelperError.TimeParseFailed("系统版本过低，不支持此功能 (需 Android 8.0+)")
        }

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        try {
            val startDate = LocalDateTime.parse("${list[0]} ${timeList[0]}", dateFormatter)
            val endDate = LocalDateTime.parse("${list[0]} ${timeList[1]}", dateFormatter)
            // Log.d(TAG, "startTime:$startDate, endTime:$endDate")
            return Pair(startDate, endDate)
        } catch (e: DateTimeParseException) {
            throw EduHelperError.TimeParseFailed("时间解析异常")
        }
    }

    // 修复了之前代码中 "end" 分支返回 Unit 的 Bug
    private fun getSemesterid(semesterType: String): String {
        return when (semesterType) {
            "beginning" -> "1"
            "middle" -> "2"
            "end" -> "3" // 修正：原来这里是空的，会导致参数传递错误
            else -> "3"
        }
    }

    // 获取学期列表
    private suspend fun getSemesterMessage(): ArrayList<String> {
        val response =
                api.getExamSemester().body()
                        ?: throw EduHelperError.examScheduleRetrievalFailed("获取学期列表失败：响应为空")

        val document = Jsoup.parse(response.toString())

        val semesters =
                document.select("#xnxqid").first()
                        ?: throw EduHelperError.examScheduleRetrievalFailed("未找到学期下拉框")

        val options = semesters.select("option")
        val result = ArrayList<String>()
        var defaultSemester = ""

        for (option in options) {
            val name = option.text().trim()
            if (option.hasAttr("selected")) {
                defaultSemester = name
            }
            result.add(name)
        }

        if (result.isEmpty()) {
            throw EduHelperError.availableSemestersForExamScheduleRetrievalFailed("学期列表为空")
        }

        // 逻辑保留：如果有默认选中的学期，把它加到列表前面或者用来做默认值
        // 原逻辑似乎是把它加到了列表里。这里为了保险，确保返回列表不为空
        if (defaultSemester.isNotEmpty() && !result.contains(defaultSemester)) {
            result.add(0, defaultSemester)
        } else if (defaultSemester.isNotEmpty()) {
            // 如果想让默认学期排在第一个
            result.remove(defaultSemester)
            result.add(0, defaultSemester)
        }

        return result
    }
}
