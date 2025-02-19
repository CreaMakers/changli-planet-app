package com.example.changli_planet_app.Activity.Store

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.changli_planet_app.Activity.Action.ScoreInquiryAction
import com.example.changli_planet_app.Activity.State.ScoreInquiryState
import com.example.changli_planet_app.Cache.ScoreCache
import com.example.changli_planet_app.Cache.StudentInfoManager
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Store
import com.example.changli_planet_app.Data.model.ScoreDetail
import com.example.changli_planet_app.Data.model.ScoreDetailResponse
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.GradeResponse
import com.example.changli_planet_app.Widget.Dialog.ErrorStuPasswordResponseDialog
import com.example.changli_planet_app.Widget.Dialog.NormalResponseDialog
import com.example.changli_planet_app.Widget.Dialog.ScoreDetailDialog
import com.example.changli_planet_app.Widget.View.CustomToast
import com.google.android.material.color.utilities.Score
import okhttp3.Response

class ScoreInquiryStore : Store<ScoreInquiryState, ScoreInquiryAction>() {
    var currentState = ScoreInquiryState()
    val handler = Handler(Looper.getMainLooper())
    override fun handleEvent(action: ScoreInquiryAction) {
        currentState = when (action) {
            is ScoreInquiryAction.ShowData -> {
                _state.onNext(currentState)
                currentState
            }

            is ScoreInquiryAction.UpdateGrade -> {
                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .get(PlanetApplication.ToolIp + "/grades")
                    .addQueryParam("stuNum", action.studentId)
                    .addQueryParam("password", action.password)
                    .addQueryParam("term", "")
                    .build()
                OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        try {
                            val gradeResponse = OkHttpHelper.gson.fromJson(
                                response.body?.string(),
                                GradeResponse::class.java
                            )

                            when (gradeResponse.code) {
                                "200" -> {
                                    currentState.grades = gradeResponse.data
                                    _state.onNext(currentState)
                                }

                                "403" -> {
                                    currentState.grades = emptyList()
                                    handler.post {
                                        try {
                                            ErrorStuPasswordResponseDialog(
                                                action.context,
                                                "学号或密码错误ʕ⸝⸝⸝˙Ⱉ˙ʔ",
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
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    _state.onNext(currentState)
                                }

                                else -> {
                                    currentState.grades = emptyList()
                                    _state.onNext(currentState)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(error: String) {
                        currentState.grades = emptyList()
                        _state.onNext(currentState)
                    }

                })
                currentState
            }

            ScoreInquiryAction.initilaize -> {
                currentState.grades = emptyList()
                _state.onNext(currentState)
                currentState
            }

            is ScoreInquiryAction.GetScoreDetail -> {
                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .get(PlanetApplication.ToolIp + "/grades/detail")
                    .addQueryParam("stuNum", StudentInfoManager.studentId)
                    .addQueryParam("password", StudentInfoManager.studentPassword)
                    .addQueryParam("pscjUrl", action.pscjUrl)
                    .build()
                OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        try {
                            val gradeResponse = OkHttpHelper.gson.fromJson(
                                response.body?.string(),
                                ScoreDetailResponse::class.java
                            )

                            when (gradeResponse.code) {
                                "200" -> {
                                    showScoreDetailDialog(
                                        action.context,
                                        gradeResponse.data,
                                        action.courseName,
                                        action.pscjUrl
                                    )
                                    _state.onNext(currentState)
                                }

                                "403" -> {
//                                    handler.post {
//                                        try {
//                                            ErrorStuPasswordResponseDialog(
//                                                action.context,
//                                                "学号或密码错误ʕ⸝⸝⸝˙Ⱉ˙ʔ",
//                                                "查询失败"
//                                            ).show()
//                                        } catch (e: Exception) {
//                                            e.printStackTrace()
//                                        }
//                                    }
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
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    _state.onNext(currentState)
                                }

                                else -> {
                                    currentState.grades = emptyList()
                                    _state.onNext(currentState)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(error: String) {
                        handler.post {
                            CustomToast.showMessage(action.context, "获取成绩详细失败")
                        }
                        _state.onNext(currentState)
                    }
                })
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
                ScoreDetailDialog(
                    context = context,
                    content = "暂无平时成绩",
                    titleContent = courseName
                ).show()
            }
        } else {
            val detailsString = contentBuilder.toString()
            ScoreCache.saveGradesDetailByUrl(pscjUrl, detailsString)
            handler.post {
                ScoreDetailDialog(
                    context = context,
                    content = detailsString,
                    titleContent = courseName
                ).show()
            }
        }
    }

}