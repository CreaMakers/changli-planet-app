package com.example.changli_planet_app.Network.NetApi

import android.devicelock.DeviceId
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Data.jsonbean.CommonResult
import com.example.changli_planet_app.Network.Response.FreshNews
import com.example.changli_planet_app.Network.Response.FreshNewsItem
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File


interface FreshNewsApi {
    @Multipart
    @POST("fresh_news")
    suspend fun postFreshNews(
        @Part images:List<MultipartBody.Part>,
        @Part("fresh_news") freshNews: RequestBody,
        @Header("deviceId") deviceId:String=PlanetApplication.deviceId,
        @Header("Authorization") authorization:String=PlanetApplication.accessToken?:"",
    ): CommonResult<FreshNewsItem>
}

fun File.toImagePart(partName:String):MultipartBody.Part=MultipartBody.Part.createFormData(
    name = partName,
    filename = this.name,
    body = this.asRequestBody("image/*".toMediaType())
)
