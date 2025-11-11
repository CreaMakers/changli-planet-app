package com.creamaker.changli_planet_app.feature.common.redux.store

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.dcelysia.csust_spider.education.data.remote.EducationHelper
import com.dcelysia.csust_spider.education.data.remote.model.CourseGrade
import com.dcelysia.csust_spider.education.data.remote.model.GradeDetail
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.Store
import com.creamaker.changli_planet_app.feature.common.data.local.entity.Grade
import com.creamaker.changli_planet_app.feature.common.data.local.mmkv.ScoreCache
import com.creamaker.changli_planet_app.feature.common.data.remote.dto.ScoreDetail
import com.creamaker.changli_planet_app.feature.common.redux.action.ScoreInquiryAction
import com.creamaker.changli_planet_app.feature.common.redux.state.ScoreInquiryState
import com.creamaker.changli_planet_app.widget.dialog.ErrorStuPasswordResponseDialog
import com.creamaker.changli_planet_app.widget.dialog.NormalResponseDialog
import com.creamaker.changli_planet_app.widget.dialog.ScoreDetailDialog
import com.creamaker.changli_planet_app.widget.view.CustomToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ScoreInquiryStore : Store<ScoreInquiryState, ScoreInquiryAction>() {
    private val TAG = "ScoreInquiryStore"
    var currentState = ScoreInquiryState()
    val handler = Handler(Looper.getMainLooper())
    override fun handleEvent(action: ScoreInquiryAction) {
        currentState = when (action) {
            is ScoreInquiryAction.ShowData -> {
                _state.onNext(currentState)
                currentState
            }

            is ScoreInquiryAction.UpdateGrade -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val result = EducationHelper.getCourseGrades()

                    when(result?.code) {
                        "200" -> {
                            currentState.grades = result?.data?.map { it.toGrade() } ?: emptyList()
                            _state.onNext(currentState)
                            handler.post {
                                CustomToast.Companion.showMessage(PlanetApplication.Companion.appContext,"刷新成功")
                            }
                        }

                        "403" -> {
                            currentState.grades = emptyList()
                            handler.post {
                                try {
                                    ErrorStuPasswordResponseDialog(
                                        action.context,
                                        "学号或密码错误ʕ⸝⸝⸝˙Ⱉ˙ʔ",
                                        "查询失败",
                                        action.refresh
                                    ).show()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            _state.onNext(currentState)
                        }

                        "404" -> {
                            currentState.grades = emptyList()
                            handler.post {
                                try {
                                    NormalResponseDialog(
                                        action.context,
                                        "网络出现波动啦！请重新刷新~₍ᐢ..ᐢ₎♡",
                                        "查询失败"
                                    ).show()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        else -> {
                            currentState.grades = emptyList()
                            _state.onNext(currentState)
                        }
                    }
                }
//                val httpUrlHelper = HttpUrlHelper.HttpRequest()
//                    .get(PlanetApplication.Companion.ToolIp + "/grades")
//                    .addQueryParam("stuNum", action.studentId)
//                    .addQueryParam("password", action.password)
//                    .addQueryParam("term", "")
//                    .build()
//                OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
//                    override fun onSuccess(response: Response) {
//                        try {
//                            val gradeResponse = OkHttpHelper.gson.fromJson(
//                                response.body?.string(),
//                                GradeResponse::class.java
//                            )
//
//                            when (gradeResponse.code) {
//                                "200" -> {
//                                    currentState.grades = gradeResponse.data
//                                    _state.onNext(currentState)
//                                    handler.post{
//                                        CustomToast.Companion.showMessage(PlanetApplication.Companion.appContext,"刷新成功")
//                                    }
//                                }
//
//                                "403" -> {
//                                    currentState.grades = emptyList()
//                                    handler.post {
//                                        try {
//                                            ErrorStuPasswordResponseDialog(
//                                                action.context,
//                                                "学号或密码错误ʕ⸝⸝⸝˙Ⱉ˙ʔ",
//                                                "查询失败",
//                                                action.refresh
//                                            ).show()
//                                        } catch (e: Exception) {
//                                            e.printStackTrace()
//                                        }
//                                    }
//                                    _state.onNext(currentState)
//                                }
//
//                                "404" -> {
//                                    currentState.grades = emptyList()
//                                    handler.post {
//                                        try {
//                                            NormalResponseDialog(
//                                                action.context,
//                                                "网络出现波动啦！请重新刷新~₍ᐢ..ᐢ₎♡",
//                                                "查询失败"
//                                            ).show()
//                                        } catch (e: Exception) {
//                                            e.printStackTrace()
//                                        }
//                                    }
//                                    _state.onNext(currentState)
//                                }
//
//                                else -> {
//                                    currentState.grades = emptyList()
//                                    _state.onNext(currentState)
//                                }
//                            }
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    }
//
//                    override fun onFailure(error: String) {
//                        currentState.grades = emptyList()
//                        _state.onNext(currentState)
//                    }
//
//                })
                currentState
            }

            ScoreInquiryAction.initilaize -> {
                currentState.grades = emptyList()
                _state.onNext(currentState)
                currentState
            }

            is ScoreInquiryAction.GetScoreDetail -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val result = EducationHelper.getGradeDetail(action.pscjUrl)

                    when(result?.code) {
                        "200" -> {
                            result?.data?.toScoreDetail()?.let { scoreDetail ->
                                showScoreDetailDialog(
                                    action.context,
                                    scoreDetail,
                                    action.courseName,
                                    action.pscjUrl
                                )
                            }
                            _state.onNext(currentState)
                        }

                        "403" -> {
                            handler.post {
                                try {
                                    NormalResponseDialog(
                                        action.context,
                                        "获取教务系统cookie失败！请重新刷新~₍ᐢ..ᐢ₎♡",
                                        "查询失败"
                                    ).show()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            _state.onNext(currentState)
                        }

                        "404" -> {
                            currentState.grades = emptyList()
                            handler.post {
                                try {
                                    NormalResponseDialog(
                                        action.context,
                                        "网络出现波动啦！请重新刷新~₍ᐢ..ᐢ₎♡",
                                        "查询失败"
                                    ).show()
                                } catch (e: Exception) { e.printStackTrace()
                                }
                            }
                            _state.onNext(currentState)
                        }

                        else -> {
                            currentState.grades = emptyList()
                            _state.onNext(currentState)
                        }
                    }
                }
//                val httpUrlHelper = HttpUrlHelper.HttpRequest()
//                    .get(PlanetApplication.Companion.ToolIp + "/grades/detail")
//                    .addQueryParam("stuNum", StudentInfoManager.studentId)
//                    .addQueryParam("password", StudentInfoManager.studentPassword)
//                    .addQueryParam("pscjUrl", action.pscjUrl)
//                    .build()
//                OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
//                    override fun onSuccess(response: Response) {
//                        try {
//                            val gradeResponse = OkHttpHelper.gson.fromJson(
//                                response.body?.string(),
//                                ScoreDetailResponse::class.java
//                            )
//
//                            when (gradeResponse.code) {
//                                "200" -> {
//                                    showScoreDetailDialog(
//                                        action.context,
//                                        gradeResponse.data,
//                                        action.courseName,
//                                        action.pscjUrl
//                                    )
//                                    _state.onNext(currentState)
//                                }
//
//                                "403" -> {
////                                    handler.post {
////                                        try {
////                                            ErrorStuPasswordResponseDialog(
////                                                action.context,
////                                                "学号或密码错误ʕ⸝⸝⸝˙Ⱉ˙ʔ",
////                                                "查询失败"
////                                            ).show()
////                                        } catch (e: Exception) {
////                                            e.printStackTrace()
////                                        }
////                                    }
//                                    handler.post {
//                                        try {
//                                            NormalResponseDialog(
//                                                action.context,
//                                                "获取教务系统cookie失败！请重新刷新~₍ᐢ..ᐢ₎♡",
//                                                "查询失败"
//                                            ).show()
//                                        } catch (e: Exception) {
//                                            e.printStackTrace()
//                                        }
//                                    }
//                                    _state.onNext(currentState)
//                                }
//
//                                "404" -> {
//                                    currentState.grades = emptyList()
//                                    handler.post {
//                                        try {
//                                            NormalResponseDialog(
//                                                action.context,
//                                                "网络出现波动啦！请重新刷新~₍ᐢ..ᐢ₎♡",
//                                                "查询失败"
//                                            ).show()
//                                        } catch (e: Exception) {
//                                            e.printStackTrace()
//                                        }
//                                    }
//                                    _state.onNext(currentState)
//                                }
//
//                                else -> {
//                                    currentState.grades = emptyList()
//                                    _state.onNext(currentState)
//                                }
//                            }
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    }
//
//                    override fun onFailure(error: String) {
//                        handler.post {
//                            CustomToast.Companion.showMessage(action.context, "获取成绩详细失败")
//                        }
//                        _state.onNext(currentState)
//                    }
//                })
                currentState
            }
        }

    }

    private fun showScoreDetailDialog(
        context: Context,
        scoreDetail: ScoreDetail,
        courseName: String,
        pscjUrl: String,
    ) {
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

        if (!flag) {
            handler.post {
//                ScoreDetailDialog(
//                    context = context,
//                    content = "暂无平时成绩",
//                    titleContent = courseName
//                ).show()
                ScoreDetailDialog.Companion.showDialog(context,"暂无平时成绩",courseName)
            }
        } else {
            val detailsString = contentBuilder.toString()
            ScoreCache.saveGradesDetailByUrl(pscjUrl, detailsString)
            handler.post {
//                ScoreDetailDialog(
//                    context = context,
//                    content = detailsString,
//                    titleContent = courseName
//                ).show()
                ScoreDetailDialog.Companion.showDialog(context,detailsString,courseName)
            }
        }
    }

    fun CourseGrade.toGrade(): Grade {
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

    fun GradeDetail.toScoreDetail(): ScoreDetail {
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