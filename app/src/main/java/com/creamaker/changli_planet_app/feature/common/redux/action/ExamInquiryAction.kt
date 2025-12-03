package com.creamaker.changli_planet_app.feature.common.redux.action

import android.content.Context
import kotlinx.coroutines.CoroutineScope

/**
 * 考试场次查询
 */
sealed class ExamInquiryAction {
    object initilaize : ExamInquiryAction()
    data class UpdateExamData(
        val scope: CoroutineScope,
        val termTime: String,
        val refresh: () -> Unit,
        val onSuccess:() -> Unit,
        val onError:(String) -> Unit
    ) : ExamInquiryAction()
}