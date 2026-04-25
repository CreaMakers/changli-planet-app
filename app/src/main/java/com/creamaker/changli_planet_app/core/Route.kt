package com.creamaker.changli_planet_app.core

import android.content.Context
import android.content.Intent
import com.creamaker.changli_planet_app.common.ui.WebViewActivity
import com.creamaker.changli_planet_app.feature.common.ui.CalendarActivity
import com.creamaker.changli_planet_app.feature.common.ui.CampusMapActivity
import com.creamaker.changli_planet_app.feature.common.ui.CetActivity
import com.creamaker.changli_planet_app.feature.common.ui.ClassInfoActivity
import com.creamaker.changli_planet_app.feature.common.ui.ContractActivity
import com.creamaker.changli_planet_app.feature.common.ui.ElectronicActivity
import com.creamaker.changli_planet_app.feature.common.ui.ExamArrangementActivity
import com.creamaker.changli_planet_app.feature.common.ui.MandeActivity
import com.creamaker.changli_planet_app.feature.common.ui.ScoreInquiryActivity
import com.creamaker.changli_planet_app.feature.ledger.ui.AccountBookActivity
import com.creamaker.changli_planet_app.feature.ledger.ui.AddSomethingAccountActivity
import com.creamaker.changli_planet_app.feature.ledger.ui.FixSomethingAccountActivity
import com.creamaker.changli_planet_app.feature.mooc.ui.MoocActivity
import com.creamaker.changli_planet_app.feature.timetable.ui.TimeTableActivity
import com.creamaker.changli_planet_app.freshNews.ui.PublishFreshNewsActivity
import com.creamaker.changli_planet_app.freshNews.ui.UserHomeActivity
import com.creamaker.changli_planet_app.profileSettings.ui.AboutActivity
import com.creamaker.changli_planet_app.settings.ui.BindingUserActivity
import com.creamaker.changli_planet_app.skin.ui.SkinSelectionActivity

/**
 * 所有页面跳转逻辑都应卸载Route中，方便统一管理
 * 使用方法：Route.goxx()
 *
 * 目前应用唯一的登录入口为 BindingUserActivity（绑定学号）。
 */
object Route {

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

    fun goCalendar(context: Context) {
        val intent = Intent(context, CalendarActivity::class.java)
        context.startActivity(intent)
    }

    fun goSkinSecletion(context: Context) {
        val intent = Intent(context, SkinSelectionActivity::class.java)
        context.startActivity(intent)
    }
}
