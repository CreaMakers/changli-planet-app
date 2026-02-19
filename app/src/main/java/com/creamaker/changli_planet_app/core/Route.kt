package com.creamaker.changli_planet_app.core

import android.content.Context
import android.content.Intent
import com.creamaker.changli_planet_app.auth.ui.BindEmailActivity
import com.creamaker.changli_planet_app.auth.ui.ForgetPasswordActivity
import com.creamaker.changli_planet_app.auth.ui.LoginActivity
import com.creamaker.changli_planet_app.auth.ui.LoginByEmailActivity
import com.creamaker.changli_planet_app.auth.ui.RegisterActivity
import com.creamaker.changli_planet_app.common.ui.WebViewActivity
import com.creamaker.changli_planet_app.feature.common.ui.CalendarActivity
import com.creamaker.changli_planet_app.feature.common.ui.CampusMapActivity
import com.creamaker.changli_planet_app.feature.common.ui.CetActivity
//import com.creamaker.changli_planet_app.feature.common.ui.ClassInfoActivity
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
import com.creamaker.changli_planet_app.settings.ui.AccountSecurityActivity
import com.creamaker.changli_planet_app.settings.ui.BindingUserActivity
import com.creamaker.changli_planet_app.settings.ui.ChangeEmailActivity
import com.creamaker.changli_planet_app.settings.ui.UserProfileActivity
import com.creamaker.changli_planet_app.skin.ui.SkinSelectionActivity

/**
 * 所有页面跳转逻辑都应卸载Route中，方便统一管理
 * 使用方法：Route.goxx()
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


//    fun goClassInfo(context: Context) {
//        val intent = Intent(context, ClassInfoActivity::class.java)
//        context.startActivity(intent)
//    }

    fun goLogin(context: Context) {
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
    }

    fun goUserProfile(context: Context) {
        val intent = Intent(context, UserProfileActivity::class.java)
        context.startActivity(intent)
    }

    fun goLoginFromRegister(context: Context, name: String, password: String) {
        val intent = Intent(context, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtra("username", name)
        intent.putExtra("password", password)
        context.startActivity(intent)
    }

    fun goLoginForcibly(context: Context) {
        val intent = Intent(context, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }

    fun goLoginByEmail(context: Context) {
        val intent = Intent(context, LoginByEmailActivity::class.java)
        context.startActivity(intent)
    }

    fun goRegister(context: Context) {
        val intent = Intent(context, RegisterActivity::class.java)
        context.startActivity(intent)
    }

    fun goBindEmailFromRegister(context: Context, name: String, password: String) {
        val intent = Intent(context, BindEmailActivity::class.java)
        intent.putExtra("username", name)
        intent.putExtra("password", password)
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

    fun goBindingUser(context: Context) {
        val intent = Intent(context, BindingUserActivity::class.java)
        context.startActivity(intent)
    }

    fun goAccountSecurity(context: Context) {
        val intent = Intent(context, AccountSecurityActivity::class.java)
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

    fun goForgetPassword(context: Context) {
        val intent = Intent(context, ForgetPasswordActivity::class.java)
        context.startActivity(intent)
    }

    fun goChangeEmail(context: Context) {
        val intent = Intent(context, ChangeEmailActivity::class.java)
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
    fun goSkinSecletion(context: Context){
        val intent = Intent(context, SkinSelectionActivity::class.java)
        context.startActivity(intent)
    }
}