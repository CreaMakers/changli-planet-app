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
}
