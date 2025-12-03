package com.creamaker.changli_planet_app.feature.common.redux.action

import com.creamaker.changli_planet_app.feature.common.data.remote.dto.ScoreDetail
import kotlinx.coroutines.CoroutineScope

sealed class ScoreInquiryAction {
    object ShowData : ScoreInquiryAction()
    object Initialize : ScoreInquiryAction()

    /**
     * @param scope: 传入 Activity 的 lifecycleScope
     * @param onAuthError: 密码错误时的回调 (UI操作)
     * @param onNetError: 网络错误时的回调 (UI操作)
     * @param onSuccess: 成功时的回调 (UI操作，如 Toast)
     */
    data class UpdateGrade(
        val scope: CoroutineScope,
        val studentId: String,
        val password: String,
        val onAuthError: () -> Unit,
        val onNetError: () -> Unit,
        val onSuccess: () -> Unit
    ) : ScoreInquiryAction()

    /**
     * @param onShowDetail: 成功获取详情后，回调给 Activity 显示弹窗
     */
    data class GetScoreDetail(
        val scope: CoroutineScope,
        val pscjUrl: String,
        val courseName: String,
        val onShowDetail: (details: String) -> Unit, // Store 处理完字符串后传回给 Activity
        val onAuthError: () -> Unit,
        val onNetError: () -> Unit
    ) : ScoreInquiryAction()
}