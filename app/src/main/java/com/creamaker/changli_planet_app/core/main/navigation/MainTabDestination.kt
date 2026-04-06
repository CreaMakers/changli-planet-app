package com.creamaker.changli_planet_app.core.main.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.creamaker.changli_planet_app.R

sealed class MainTabDestination(
    val index: Int,
    @param:DrawableRes @get:DrawableRes val iconResId: Int,
    @param:StringRes @get:StringRes val labelResId: Int
) : NavKey {

    data object Overview : MainTabDestination(
        index = 0,
        iconResId = R.drawable.ic_overview,
        labelResId = R.string.overview
    )

    data object Feature : MainTabDestination(
        index = 1,
        iconResId = R.drawable.nfeature,
        labelResId = R.string.function
    )

    data object News : MainTabDestination(
        index = 2,
        iconResId = R.drawable.nnews,
        labelResId = R.string.intel_station
    )

    data object Profile : MainTabDestination(
        index = 3,
        iconResId = R.drawable.nprofile,
        labelResId = R.string.profile_home
    )

    companion object {
        fun fromIndex(index: Int): MainTabDestination? = when (index) {
            0 -> Overview
            1 -> Feature
            2 -> News
            3 -> Profile
            else -> null
        }
    }
}
