package com.creamaker.changli_planet_app.feature.common.redux.store

import com.creamaker.changli_planet_app.core.Store
import com.creamaker.changli_planet_app.feature.common.data.local.entity.Grade
import com.creamaker.changli_planet_app.feature.common.data.local.mmkv.ScoreCache
import com.creamaker.changli_planet_app.feature.common.redux.action.ScoreInquiryAction
import com.creamaker.changli_planet_app.feature.common.redux.state.ScoreInquiryState
import com.dcelysia.csust_spider.education.data.remote.EducationHelper
import com.dcelysia.csust_spider.education.data.remote.model.CourseGrade
import com.dcelysia.csust_spider.education.data.remote.model.GradeDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScoreInquiryStore : Store<ScoreInquiryState, ScoreInquiryAction>() {
    private val TAG = "ScoreInquiryStore"
    var currentState = ScoreInquiryState()

    override fun handleEvent(action: ScoreInquiryAction) {
        when (action) {
            is ScoreInquiryAction.ShowData -> {
                _state.onNext(currentState)
            }

            is ScoreInquiryAction.UpdateGrade -> {
                // 使用 Activity 传进来的 lifecycleScope
                action.scope.launch(Dispatchers.IO) {
                    val result = EducationHelper.getCourseGrades()

                    withContext(Dispatchers.Main) {
                        when (result?.code) {
                            "200" -> {
                                currentState.grades = result.data?.map { it.toGrade() } ?: emptyList()
                                _state.onNext(currentState)
                                action.onSuccess() // 回调 Activity 显示 Toast
                            }
                            "403" -> {
                                currentState.grades = emptyList()
                                _state.onNext(currentState)
                                action.onAuthError() // 回调 Activity 显示 Dialog
                            }
                            "404", null -> {
                                currentState.grades = emptyList()
                                _state.onNext(currentState)
                                action.onNetError() // 回调 Activity 显示 Dialog
                            }
                            else -> {
                                currentState.grades = emptyList()
                                _state.onNext(currentState)
                            }
                        }
                    }
                }
            }

            ScoreInquiryAction.Initialize -> {
                currentState.grades = emptyList()
                _state.onNext(currentState)
            }

            is ScoreInquiryAction.GetScoreDetail -> {
                action.scope.launch(Dispatchers.IO) {
                    val result = EducationHelper.getGradeDetail(action.pscjUrl)

                    withContext(Dispatchers.Main) {
                        when (result?.code) {
                            "200" -> {
                                result.data?.let {
                                    val detailString = buildScoreDetailString(it.toScoreDetail())
                                    // 缓存逻辑保留在 Store 中
                                    ScoreCache.saveGradesDetailByUrl(action.pscjUrl, detailString)
                                    // 将处理好的字符串回调给 Activity 进行显示
                                    action.onShowDetail(detailString)
                                }
                                _state.onNext(currentState)
                            }
                            "403" -> {
                                action.onAuthError()
                                _state.onNext(currentState)
                            }
                            "404", null -> {
                                action.onNetError()
                                _state.onNext(currentState)
                            }
                            else -> {
                                _state.onNext(currentState)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- 辅助方法保留在 Store 中 ---

    private fun buildScoreDetailString(scoreDetail: com.creamaker.changli_planet_app.feature.common.data.remote.dto.ScoreDetail): String {
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

    private fun GradeDetail.toScoreDetail(): com.creamaker.changli_planet_app.feature.common.data.remote.dto.ScoreDetail {
        val componentMap = components.associateBy { it.type }
        return com.creamaker.changli_planet_app.feature.common.data.remote.dto.ScoreDetail(
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