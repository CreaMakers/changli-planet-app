package com.example.changli_planet_app.Activity.State

import com.example.changli_planet_app.Network.Resource
import com.example.changli_planet_app.Network.Response.UserProfile

data class UserState (
    var userProfile: Resource<UserProfile> = Resource.Loading()
)