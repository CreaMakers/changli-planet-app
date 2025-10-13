package com.creamaker.changli_planet_app.common.redux.state

import com.creamaker.changli_planet_app.common.data.remote.dto.UserProfile
import com.creamaker.changli_planet_app.common.data.remote.dto.UserStats

data class UserState (
    var userProfile: UserProfile = UserProfile(),
    var userStats: UserStats = UserStats(),
    var avatarUri: String = "",
    var locationChangedManually : Boolean = false,
    var uiForLoading: Boolean = true
)