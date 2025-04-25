package com.example.changli_planet_app.Core

import android.content.Context
import android.content.Intent
import com.example.changli_planet_app.Activity.AccountSecurityActivity
import com.example.changli_planet_app.Activity.BindEmailActivity
import com.example.changli_planet_app.Activity.BindingUserActivity
import com.example.changli_planet_app.Activity.CampusMapActivity
import com.example.changli_planet_app.Activity.CetActivity
import com.example.changli_planet_app.Activity.ClassInfoActivity
import com.example.changli_planet_app.Activity.ContractActivity
import com.example.changli_planet_app.Activity.ElectronicActivity
import com.example.changli_planet_app.Activity.ExamArrangementActivity
import com.example.changli_planet_app.Activity.LoginActivity
import com.example.changli_planet_app.Activity.LoginByEmailActivity
import com.example.changli_planet_app.Activity.LoseActivity
import com.example.changli_planet_app.Activity.MainActivity
import com.example.changli_planet_app.Activity.MandeActivity
import com.example.changli_planet_app.Activity.PublishFoundThingActivity
import com.example.changli_planet_app.Activity.PublishFreshNewsActivity
import com.example.changli_planet_app.Activity.PublishLoseThingActivity
import com.example.changli_planet_app.Activity.RegisterActivity
import com.example.changli_planet_app.Activity.ScoreInquiryActivity
import com.example.changli_planet_app.Activity.TimeTableActivity
import com.example.changli_planet_app.Activity.UserProfileActivity

/**
 * 所有页面跳转逻辑都应卸载Route中，方便统一管理
 * 使用方法：Route.goxx()
 */
object Route {

    fun goCampusMap(context: Context) {
        val intent = Intent(context, CampusMapActivity::class.java)
        context.startActivity(intent)
    }

    fun goClassInfo(context: Context) {
        val intent = Intent(context, ClassInfoActivity::class.java)
        context.startActivity(intent)
    }

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

    fun goLoginByEmailForcibly(context: Context){
        val intent = Intent(context, LoginByEmailActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }

    fun goRegister(context: Context) {
        val intent = Intent(context, RegisterActivity::class.java)
        context.startActivity(intent)
    }

    fun goBindEmailFromRegister(context: Context,name: String,password: String){
        val intent=Intent(context,BindEmailActivity::class.java)
        intent.putExtra("username",name)
        intent.putExtra("password",password)
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


    fun goLose(context: Context) {
        val intent = Intent(context, LoseActivity::class.java)
        context.startActivity(intent)
    }

    fun goScoreInquiry(context: Context) {
        val intent = Intent(context, ScoreInquiryActivity::class.java)
        context.startActivity(intent)
    }

    fun goPublishLoseThing(context: Context) {
        val intent = Intent(context, PublishLoseThingActivity::class.java)
        context.startActivity(intent)
    }

    fun goPublishFoundThing(context: Context) {
        val intent = Intent(context, PublishFoundThingActivity::class.java)
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

    fun goContract(context: Context){
        val intent=Intent(context,ContractActivity::class.java)
        context.startActivity(intent)
    }

}