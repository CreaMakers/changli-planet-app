package com.example.changli_planet_app.Cache.Room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "top_card")
data class TopCardEntity(
    @PrimaryKey val username: String,
    val allNumber: Int,
    val totalMoney: Double,
    val dailyAverage: Double
)