package com.creamaker.changli_planet_app.feature.common.viewModel

import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.common.data.local.mmkv.StudentInfoManager
import com.creamaker.changli_planet_app.core.mvi.MviViewModel
import com.creamaker.changli_planet_app.feature.common.contract.ScoreInquiryContract
import com.creamaker.changli_planet_app.feature.common.data.local.entity.Grade
import com.creamaker.changli_planet_app.feature.common.data.local.mmkv.ScoreCache
import com.creamaker.changli_planet_app.feature.common.data.remote.dto.ScoreDetail
import com.dcelysia.csust_spider.education.data.remote.EducationHelper
import com.dcelysia.csust_spider.education.data.remote.model.CourseGrade
import com.dcelysia.csust_spider.education.data.remote.model.GradeDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScoreInquiryViewModel :
    MviViewModel<ScoreInquiryContract.Intent, ScoreInquiryContract.State>() {

    private val _effect = Channel<ScoreInquiryContract.Effect>(Channel.BUFFERED)
    val effect: Flow<ScoreInquiryContract.Effect> = _effect.receiveAsFlow()

    override fun initialState(): ScoreInquiryContract.State = ScoreInquiryContract.State()

    override fun processIntent(intent: ScoreInquiryContract.Intent) {
        when (intent) {
            is ScoreInquiryContract.Intent.InitialLoad -> loadCachedData()
            is ScoreInquiryContract.Intent.RefreshData -> refreshData(intent.force)
            is ScoreInquiryContract.Intent.FetchScoreDetail -> fetchScoreDetail(
                intent.pscjUrl,
                intent.courseName
            )
        }
    }

    private fun loadCachedData() {
        val cachedGrades = ScoreCache.getGrades()
        if (cachedGrades != null && cachedGrades.isNotEmpty()) {
            updateState { copy(grades = cachedGrades) }
        } else {
            refreshData(force = true)
        }
    }

    private fun refreshData(force: Boolean) {
        val studentId = StudentInfoManager.studentId
        val studentPassword = StudentInfoManager.studentPassword

        if (studentId.isEmpty() || studentPassword.isEmpty()) {
            sendEffect(ScoreInquiryContract.Effect.ShowToast("请先绑定学号和密码"))
            return
        }

        updateState { copy(isLoading = true) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = EducationHelper.getCourseGrades()

                withContext(Dispatchers.Main) {
                    when (result?.code) {
                        "200" -> {
                            val grades = result.data?.map { it.toGrade() } ?: emptyList()
                            updateState { copy(isLoading = false, grades = grades) }
                            sendEffect(ScoreInquiryContract.Effect.ShowToast("刷新成功"))
                        }

                        "403" -> {
                            updateState { copy(isLoading = false) }
                            sendEffect(
                                ScoreInquiryContract.Effect.ShowAuthErrorDialog(
                                    "查询失败",
                                    "学号或密码错误ʕ⸝⸝⸝˙Ⱉ˙ʔ"
                                )
                            )
                        }

                        "404", null -> {
                            updateState { copy(isLoading = false) }
                            sendEffect(
                                ScoreInquiryContract.Effect.ShowNetErrorDialog(
                                    "查询失败",
                                    "网络出现波动啦！请重新刷新~₍ᐢ..ᐢ₎♡"
                                )
                            )
                        }

                        else -> {
                            updateState { copy(isLoading = false) }
                            sendEffect(ScoreInquiryContract.Effect.ShowToast("刷新失败: ${result.msg}"))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    updateState { copy(isLoading = false) }
                    sendEffect(ScoreInquiryContract.Effect.ShowToast("发生错误: ${e.message}"))
                }
            }
        }
    }

    private fun fetchScoreDetail(pscjUrl: String, courseName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = EducationHelper.getGradeDetail(pscjUrl)
                withContext(Dispatchers.Main) {
                    when (result?.code) {
                        "200" -> {
                            result.data?.let {
                                val detailString = buildScoreDetailString(it.toScoreDetail())
                                ScoreCache.saveGradesDetailByUrl(pscjUrl, detailString)
                                sendEffect(
                                    ScoreInquiryContract.Effect.ShowDetailDialog(
                                        detailString,
                                        courseName
                                    )
                                )
                            }
                        }

                        "403" -> {
                            sendEffect(
                                ScoreInquiryContract.Effect.ShowAuthErrorDialog(
                                    "查询失败",
                                    "获取教务系统cookie失败！请重新刷新~₍ᐢ..ᐢ₎♡"
                                )
                            )
                        }

                        "404", null -> {
                            sendEffect(
                                ScoreInquiryContract.Effect.ShowNetErrorDialog(
                                    "查询失败",
                                    "网络出现波动啦！请重新刷新~₍ᐢ..ᐢ₎♡"
                                )
                            )
                        }

                        else -> {
                            sendEffect(ScoreInquiryContract.Effect.ShowToast("获取详情失败"))
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    sendEffect(ScoreInquiryContract.Effect.ShowToast("发生错误: ${e.message}"))
                }
            }
        }
    }

    private fun sendEffect(effect: ScoreInquiryContract.Effect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    // --- Helpers (moved from Store) ---

    private fun buildScoreDetailString(scoreDetail: ScoreDetail): String {
        val contentBuilder = StringBuilder()
        var flag = false
        with(scoreDetail) {
            sjcj?.let {
                flag = true
                contentBuilder.append("上机成绩：$it\n")
                contentBuilder.append("上机成绩比例：$sjcjBL\n\n")
            }
            pscj?.let {
                flag = true
                contentBuilder.append("平时成绩：$it\n")
                contentBuilder.append("平时成绩比例：$pscjBL\n\n")
            }
            qzcj?.let {
                flag = true
                contentBuilder.append("期中成绩：$it\n")
                contentBuilder.append("期中成绩比例：$qzcjBL\n\n")
            }
            qmcj?.let {
                flag = true
                contentBuilder.append("期末成绩：$it\n")
                contentBuilder.append("期末成绩比例：$qmcjBL\n\n")
            }
            contentBuilder.append("总成绩：$score")
        }
        return if (!flag) "暂无平时成绩" else contentBuilder.toString()
    }

    private fun CourseGrade.toGrade(): Grade {
        return Grade(
            id = courseID,
            item = semester,
            name = courseName,
            grade = grade.toString(),
            flag = gradeIdentifier,
            score = credit.toString(),
            timeR = totalHours.toString(),
            point = gradePoint.toString(),
            upperReItem = retakeSemester,
            method = assessmentMethod,
            property = examNature,
            attribute = courseAttribute,
            reItem = groupName,
            pscjUrl = gradeDetailUrl
        )
    }

    private fun GradeDetail.toScoreDetail(): ScoreDetail {
        val componentMap = components.associateBy { it.type }
        return ScoreDetail(
            pscj = componentMap["平时成绩"]?.grade?.toString(),
            pscjBL = componentMap["平时成绩"]?.ratio?.toString(),
            qzcj = componentMap["期中成绩"]?.grade?.toString(),
            qzcjBL = componentMap["期中成绩"]?.ratio?.toString(),
            qmcj = componentMap["期末成绩"]?.grade?.toString(),
            qmcjBL = componentMap["期末成绩"]?.ratio?.toString(),
            sjcj = componentMap["上机成绩"]?.grade?.toString(),
            sjcjBL = componentMap["上机成绩"]?.ratio?.toString(),
            score = totalGrade.toString()
        )
    }
}
