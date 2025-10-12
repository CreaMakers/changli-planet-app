package com.creamaker.changli_planet_app.freshNews.data.remote.api

import com.creamaker.changli_planet_app.common.data.remote.dto.UserProfile
import com.creamaker.changli_planet_app.freshNews.data.remote.dto.CommonResult
import retrofit2.http.GET
import retrofit2.http.Path

interface UserProfileApi {
    @GET("{user_id}/profile")
    suspend fun getUserInformationById(
        @Path("user_id") userId:Int
    ): CommonResult<UserProfile>
}