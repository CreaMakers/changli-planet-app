package com.creamaker.changli_planet_app.widget.dialog

import android.content.Context
import com.creamaker.changli_planet_app.core.Route

class GuestLimitedAccessDialog(
    context: Context
) :
    NormalChosenDialog(
        context = context,
        title = "进入未知区域了哦~",
        content = "当前功能需要登录后才能使用，请先登录！",
        confirmText = "现在登录",
        cancelText = "我再看看",
        onConfirm = { Route.goLoginForcibly(context) }
    )