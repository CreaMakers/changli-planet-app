package com.creamaker.changli_planet_app.common.redux.action

import android.content.Context

/**
 * 用户有关的 Action。
 *
 * 目前应用已取消账号系统，仅保留与学号绑定 / 应用更新相关的动作。
 */
sealed class UserAction {
    class QueryIsLastedApk(val context: Context, val versionCode: Long, val versionName: String) :
        UserAction()

    class BindingStudentNumber(
        val context: Context,
        val studentNumber: String,
        val studentPassword: String,
        val webLogin: () -> Unit
    ) : UserAction()

    class initilaize : UserAction()

    data class WebLoginSuccess(
        val context: Context,
        val account: String,
        val password: String
    ) : UserAction()

    /**
     * 静默刷新 MOOC 用户资料：用已持久化的学号/密码在后台重新跑一次 SSO 登录，
     * 拿到最新的 userName / avatar 并写回 [UserInfoManager]。
     *
     * 仅用于升级迁移等场景，不弹任何 UI，不发 FinishEvent，不清缓存，失败静默忽略。
     */
    object RefreshMoocProfileSilently : UserAction()
}
