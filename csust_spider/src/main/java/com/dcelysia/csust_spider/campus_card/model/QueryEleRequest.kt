package com.example.csustdataget.CampusCard.model

data class QueryEleRequest(
    val errmsg: String? = null,
    val aid: String?,
    val account: String = "0000001",
    val room: Room,
    val floor: Floor,
    val area: Area,
    val building: Building
) {
    data class Room(
        val roomid: String,
        val room: String
    )

    data class Floor(
        val floorid: String,
        val floor: String
    )

    data class Area(
        val area: String,
        val areaname: String
    )

    data class Building(
        val buildingid: String?,
        val building: String
    )
}
