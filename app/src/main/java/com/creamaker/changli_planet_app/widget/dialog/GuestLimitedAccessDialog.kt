package com.creamaker.changli_planet_app.widget.dialog

import android.content.Context
import com.creamaker.changli_planet_app.core.Route

class GuestLimitedAccessDialog(
    context: Context
) :
    NormalChosenDialog(
        context = context,
        title = "进入未知区域了哦~",
        content = "当前功能需要绑定学号后才能使用，现在去绑定？",
        confirmText = "去绑定",
        cancelText = "我再看看",
        onConfirm = { Route.goBindingUser(context) }
    )