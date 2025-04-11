package com.example.changli_planet_app.Network.NetApi

import com.example.changli_planet_app.Data.jsonbean.CommonResult
import com.example.changli_planet_app.Data.jsonbean.UserDisplayInformation
import com.example.changli_planet_app.Network.Response.UserProfile
import retrofit2.http.GET
import retrofit2.http.Path

interface UserProfileApi {
    @GET("{user_id}/profile")
    suspend fun getUserInformationById(
        @Path("user_id") userId:Int
    ):CommonResult<UserProfile>
}