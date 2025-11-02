package com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model

import com.google.gson.annotations.SerializedName

data class IpLocationResponse(
    val status: String?,
    val country: String?,
    val countryCode: String?,
    val region: String?,
    val regionName: String?,
    val city: String?,
    val zip: String?,
    val lat: String?,
    val lon: String?,
    val timezone: String?,
    val isp: String?,
    val org: String?,
    @SerializedName("as")
    val asInfo: String?,
    val query: String?
)