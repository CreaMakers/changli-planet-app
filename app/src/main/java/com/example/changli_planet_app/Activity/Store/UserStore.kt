package com.example.changli_planet_app.Activity.Store

import com.example.changli_planet_app.Activity.Action.UserAction
import com.example.changli_planet_app.Activity.State.UserState
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Store
import com.example.changli_planet_app.Network.HttpUrlHelper

class UserStore : Store<UserState, UserAction>() {
    private val currentState = UserState()
    override fun handleEvent(action: UserAction) {
        when (action) {
            is UserAction.GetCurrentUserProfile -> {
                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .get(PlanetApplication.UserIp + "/me/profile")
                    .build()

            }
        }
    }
}