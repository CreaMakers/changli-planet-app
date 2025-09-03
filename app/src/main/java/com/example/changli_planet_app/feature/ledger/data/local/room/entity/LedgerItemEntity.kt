package com.example.changli_planet_app.feature.ledger.data.local.room.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "something_items")
data class LedgerItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val totalMoney: Double,
    val dailyAverage: Double,
    val startTime : String,
    val picture : Int,
    val username: String
)
