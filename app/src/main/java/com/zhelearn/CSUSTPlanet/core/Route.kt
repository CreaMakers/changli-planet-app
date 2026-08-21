package com.zhelearn.CSUSTPlanet.core

import android.content.Context
import android.content.Intent
import com.zhelearn.CSUSTPlanet.common.ui.WebViewActivity
import com.zhelearn.CSUSTPlanet.feature.common.ui.CalendarActivity
import com.zhelearn.CSUSTPlanet.feature.common.ui.CampusMapActivity
import com.zhelearn.CSUSTPlanet.feature.common.ui.CetActivity
import com.zhelearn.CSUSTPlanet.feature.common.ui.ClassInfoActivity
import com.zhelearn.CSUSTPlanet.feature.common.ui.ContractActivity
import com.zhelearn.CSUSTPlanet.feature.common.ui.ElectronicActivity
import com.zhelearn.CSUSTPlanet.feature.common.ui.ExamArrangementActivity
import com.zhelearn.CSUSTPlanet.feature.common.ui.MandeActivity
import com.zhelearn.CSUSTPlanet.feature.common.ui.ScoreInquiryActivity
import com.zhelearn.CSUSTPlanet.feature.ledger.ui.AccountBookActivity
import com.zhelearn.CSUSTPlanet.feature.ledger.ui.AddSomethingAccountActivity
import com.zhelearn.CSUSTPlanet.feature.ledger.ui.FixSomethingAccountActivity
import com.zhelearn.CSUSTPlanet.feature.mooc.ui.MoocActivity
import com.zhelearn.CSUSTPlanet.feature.mooc.ui.MoocCoursePageActivity
import com.zhelearn.CSUSTPlanet.feature.timetable.ui.TimeTableActivity
import com.zhelearn.CSUSTPlanet.freshNews.ui.PublishFreshNewsActivity
import com.zhelearn.CSUSTPlanet.freshNews.ui.UserHomeActivity
import com.zhelearn.CSUSTPlanet.migration.DataMigrationActivity
import com.zhelearn.CSUSTPlanet.profileSettings.ui.AboutActivity
import com.zhelearn.CSUSTPlanet.settings.ui.BindingUserActivity
import com.zhelearn.CSUSTPlanet.skin.ui.SkinSelectionActivity

/**
 * 所有页面跳转逻辑都应卸载Route中，方便统一管理
 * 使用方法：Route.goxx()
 *
 * 目前应用唯一的登录入口为 BindingUserActivity（绑定学号）。
 */
object Route {

    fun goDataMigration(context: Context) {
        context.startActivity(Intent(context, DataMigrationActivity::class.java))
    }

    fun goCampusMap(context: Context) {
        val intent = Intent(context, CampusMapActivity::class.java)
        context.startActivity(intent)
    }

    fun goUserHomeActivity(context: Context) {
        val intent = Intent(context, UserHomeActivity::class.java)
        context.startActivity(intent)
    }

    fun goUserHomeActivity(context: Context, userId: Int) {
        val intent = Intent(context, UserHomeActivity::class.java)
        intent.putExtra("userId", userId)
        context.startActivity(intent)
    }

    fun goClassInfo(context: Context) {
        val intent = Intent(context, ClassInfoActivity::class.java)
        context.startActivity(intent)
    }

    fun goElectronic(context: Context) {
        try {
            val intent = Intent(context, ElectronicActivity::class.java)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun goHome(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)
    }

    fun goHomeForcibly(context: Context) {
        val intent = Intent(
            context,
            MainActivity::class.java
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }

    fun goTimetable(context: Context) {
        val intent = Intent(context, TimeTableActivity::class.java)
        context.startActivity(intent)
    }

    fun goScoreInquiry(context: Context) {
        val intent = Intent(context, ScoreInquiryActivity::class.java)
        context.startActivity(intent)
    }

    fun goExamArrangement(context: Context) {
        val intent = Intent(context, ExamArrangementActivity::class.java)
        context.startActivity(intent)
    }

    fun goCet(context: Context) {
        val intent = Intent(context, CetActivity::class.java)
        context.startActivity(intent)
    }

    fun goMande(context: Context) {
        val intent = Intent(context, MandeActivity::class.java)
        context.startActivity(intent)
    }

    /**
     * 应用唯一登录入口：跳转到绑定学号页面。
     *
     * @param isSwitchAccount 是否是"切换学号"场景。true 时，绑定成功后会由 BindingUserActivity 负责
     *   清理旧账号的内容缓存；进入绑定页前不会清除任何数据，避免中途放弃导致账号信息丢失。
     */
    fun goBindingUser(context: Context, isSwitchAccount: Boolean = false) {
        val intent = Intent(context, BindingUserActivity::class.java)
            .putExtra(BindingUserActivity.EXTRA_IS_SWITCH_ACCOUNT, isSwitchAccount)
        context.startActivity(intent)
    }

    /**
     * 强制清栈并跳转到绑定学号页面，用于 token 过期等场景。
     */
    fun goBindingUserForcibly(context: Context) {
        val intent = Intent(context, BindingUserActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }

    fun goPublishFreshNews(context: Context) {
        val intent = Intent(context, PublishFreshNewsActivity::class.java)
        context.startActivity(intent)
    }

    fun goContract(context: Context) {
        val intent = Intent(context, ContractActivity::class.java)
        context.startActivity(intent)
    }

    fun goAccountBook(context: Context) {
        val intent = Intent(context, AccountBookActivity::class.java)
        context.startActivity(intent)
    }

    fun goAddSomethingAccount(context: Context) {
        val intent = Intent(context, AddSomethingAccountActivity::class.java)
        context.startActivity(intent)
    }

    fun goFixSomethingAccount(context: Context, itemId: Int) {
        val intent = Intent(context, FixSomethingAccountActivity::class.java).apply {
            putExtra("ITEM_ID", itemId)
        }
        context.startActivity(intent)
    }

    fun goWebView(context: Context, url: String) {
        val intent = Intent(context, WebViewActivity::class.java).apply {
            putExtra("url_tag", url)
        }
        context.startActivity(intent)
    }

    fun goAbout(context: Context) {
        val intent = Intent(context, AboutActivity::class.java)
        context.startActivity(intent)
    }

    fun goMooc(context: Context) {
        val intent = Intent(context, MoocActivity::class.java)
        context.startActivity(intent)
    }

    /**
     * 跳转到 Mooc 课程主页 WebView（自动注入 cookie，免登录）。
     *
     * @param courseId 课程 ID（已经 `cleanCourseId()` 后的纯数字/字母串）
     */
    fun goMoocCoursePage(context: Context, courseId: String) {
        if (courseId.isBlank()) return
        val intent = Intent(context, MoocCoursePageActivity::class.java).apply {
            putExtra(MoocCoursePageActivity.EXTRA_COURSE_ID, courseId)
        }
        context.startActivity(intent)
    }

    fun goCalendar(context: Context) {
        val intent = Intent(context, CalendarActivity::class.java)
        context.startActivity(intent)
    }

    fun goSkinSecletion(context: Context) {
        val intent = Intent(context, SkinSelectionActivity::class.java)
        context.startActivity(intent)
    }
}
