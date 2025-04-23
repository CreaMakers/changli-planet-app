package com.example.changli_planet_app.Cache.Room.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "something_items")
data class SomethingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val totalMoney: Double,
    val dailyAverage: Double,
    val startTime : String,
    val picture : Int
)
