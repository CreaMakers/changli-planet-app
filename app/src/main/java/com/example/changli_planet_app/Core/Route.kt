package com.example.changli_planet_app.Core

import android.content.Context
import android.content.Intent
import com.example.changli_planet_app.Activity.ElectronicActivity
import com.example.changli_planet_app.Activity.LoginActivity
import com.example.changli_planet_app.Activity.LoseActivity
import com.example.changli_planet_app.Activity.MainActivity
import com.example.changli_planet_app.Activity.RegisterActivity

/**
 * 所有页面跳转逻辑都应卸载Route中，方便统一管理
 * 使用方法：Route.goxx()
 */
object Route {
    fun goLogin(context: Context){
        val intent = Intent(context,LoginActivity::class.java)
        context.startActivity(intent)
    }
    fun goRegister(context: Context){
        val intent = Intent(context,RegisterActivity::class.java)
        context.startActivity(intent)
    }
    fun goElectronic(context: Context){
        val intent = Intent(context,ElectronicActivity::class.java)
        context.startActivity(intent)
    }
    fun goHome(context: Context){
        val intent = Intent(context,MainActivity::class.java)
        context.startActivity(intent)
    }
    fun goLose(context: Context){
        val intent = Intent(context,LoseActivity::class.java)
        context.startActivity(intent)
    }
}