package com.creamaker.changli_planet_app.feature.common.redux.store

import android.os.Handler
import android.os.Looper
import com.dcelysia.csust_spider.education.data.remote.error.EduHelperError
import com.dcelysia.csust_spider.education.data.remote.model.ExamArrange
import com.dcelysia.csust_spider.education.data.remote.services.ExamArrangeService
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.Store
import com.creamaker.changli_planet_app.feature.common.redux.action.ExamInquiryAction
import com.creamaker.changli_planet_app.feature.common.redux.state.ExamInquiryState
import com.creamaker.changli_planet_app.widget.dialog.ErrorStuPasswordResponseDialog
import com.creamaker.changli_planet_app.widget.view.CustomToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExamArrangementStore : Store<ExamInquiryState, ExamInquiryAction>() {
    var currentState = ExamInquiryState()
    val handler = Handler(Looper.getMainLooper())
    override fun handleEvent(action: ExamInquiryAction) {
        currentState = when (action) {
            ExamInquiryAction.initilaize -> {
                currentState.exams = emptyList()
                _state.onNext(currentState)
                currentState
            }
            is ExamInquiryAction.UpdateExamData -> {
                updateExamDate(action)
                currentState
            }
        }
    }

    private fun updateExamDate(action: ExamInquiryAction.UpdateExamData) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val middle_list = ExamArrangeService.getExamArrange(action.termTime, "期中")
                val end_list = ExamArrangeService.getExamArrange(action.termTime, "期末")
                if (middle_list == null && end_list == null) {
                    handler.post {
                        ErrorStuPasswordResponseDialog(
                            action.context,
                            "暂时无法查询数据喵ʕ⸝⸝⸝˙Ⱉ˙ʔ",
                            "查询失败",
                            action.refresh
                        ).show()
                    }
                    _state.onNext(currentState)
                } else {
                    handler.post {
                        CustomToast.showMessage(PlanetApplication.appContext, "刷新成功")
                    }
                    // 合并并去重（以课程名、考试时间、校区、考场为去重键）
                    val combined = (middle_list ?: emptyList()) + (end_list ?: emptyList())
                    val deduped = combined.distinctBy { exam ->
                        listOf(
                            exam.courseNameval,
                            exam.examTime,
                            exam.campus,
                            exam.examRoomval
                        )
                    }
                    currentState.exams = deduped
                    _state.onNext(currentState)
                }
            } catch (e: EduHelperError.examScheduleRetrievalFailed) {
                handler.post {
                    ErrorStuPasswordResponseDialog(
                        action.context,
                        "未查询到考试场次表喵ʕ⸝⸝⸝˙Ⱉ˙ʔ",
                        "查询失败",
                        action.refresh
                    ).show()
                }
                _state.onNext(currentState)
            } catch (e: EduHelperError.NotLoggedIn) {
                handler.post {
                    ErrorStuPasswordResponseDialog(
                        action.context,
                        "学号或密码错误ʕ⸝⸝⸝˙Ⱉ˙ʔ",
                        "查询失败",
                        action.refresh
                    ).show()
                }
                _state.onNext(currentState)
            } catch (e: EduHelperError) {
                handler.post {
                    ErrorStuPasswordResponseDialog(
                        action.context,
                        "出现其他异常，请重试喵~",
                        "查询失败",
                        action.refresh
                    ).show()
                }
                _state.onNext(currentState)
            }
        }
    }
    }