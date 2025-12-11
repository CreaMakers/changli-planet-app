package com.example.csustdataget.CampusCard.model

import com.google.gson.annotations.SerializedName

data class ElectricityResponse(
    @SerializedName("query_elec_roominfo")
    val queryEleRoomInfo: QueryEleRequest
)
