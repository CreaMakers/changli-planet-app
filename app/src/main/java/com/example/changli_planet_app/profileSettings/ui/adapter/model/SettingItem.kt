package com.example.changli_planet_app.profileSettings.ui.adapter.model

import android.support.annotation.DrawableRes

data class SettingItem(
    val title: String,
    @DrawableRes val iconResId: Int = 0,
    val isHeader: Boolean = false,
    val actionType: Int = 0
)