package com.creamaker.changli_planet_app.feature.common.redux.store
import com.dcelysia.csust_spider.education.data.remote.services.ExamArrangeService

import com.creamaker.changli_planet_app.core.Store
import com.creamaker.changli_planet_app.feature.common.redux.action.ExamInquiryAction
import com.creamaker.changli_planet_app.feature.common.redux.state.ExamInquiryState
import com.dcelysia.csust_spider.core.Resource

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExamArrangementStore : Store<ExamInquiryState, ExamInquiryAction>() {
    var currentState = ExamInquiryState()
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
        action.scope.launch {
            val middleResult = ExamArrangeService.getExamArrange(action.termTime, "期中")
            val endResult = ExamArrangeService.getExamArrange(action.termTime, "期末")

            // 2. 判断结果状态
            // 只有当两次请求都成功 (Success) 时，才进行数据合并
            if (middleResult is Resource.Success && endResult is Resource.Success) {

                withContext(Dispatchers.Main){
                    action.onSuccess
                }

                val middleList = middleResult.data
                val endList = endResult.data

                // 合并并去重
                val combined = middleList + endList
                val deduped = combined.distinctBy { exam ->
                    listOf(
                        exam.courseNameval,
                        exam.examTime,
                        exam.campus,
                        exam.examRoomval
                    )
                }

                // 更新状态
                currentState.exams = deduped
                _state.onNext(currentState)

            } else {
                val errorMessage = when {
                    middleResult is Resource.Error -> middleResult.msg
                    endResult is Resource.Error -> endResult.msg
                    else -> "未知错误"
                }
                // 在主线程显示错误弹窗
                withContext(Dispatchers.Main){
                    action.onError(errorMessage)
                }
                _state.onNext(currentState)
            }
        }
    }
}