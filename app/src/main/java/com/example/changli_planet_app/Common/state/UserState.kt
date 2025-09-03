package com.example.changli_planet_app.Common.state

import com.example.changli_planet_app.Network.Response.UserProfile
import com.example.changli_planet_app.Network.Response.UserStats

data class UserState (
    var userProfile: UserProfile = UserProfile(),
    var userStats: UserStats = UserStats(),
    var avatarUri: String = "",
    var locationChangedManually : Boolean = false
)