package com.creamaker.changli_planet_app.feature.ledger.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "top_card")
data class LedgerTopCardEntity(
    @PrimaryKey val username: String,
    val allNumber: Int,
    val totalMoney: Double,
    val dailyAverage: Double
)