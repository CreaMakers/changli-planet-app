package com.creamaker.changli_planet_app.feature.common.data.local.room.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WeeksTypeConverter {
    val gson = Gson()

    @TypeConverter
    fun fromListToString(list: List<Int>): String? = gson.toJson(list)

    @TypeConverter
    fun fromStringToList(string: String): List<Int> {
        return gson.fromJson(string, object : TypeToken<List<Int>>() {}.type)
    }
}