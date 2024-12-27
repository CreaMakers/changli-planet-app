package com.example.changli_planet_app.Cache

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WeeksTypeConverter {
    val gson = Gson()

    @TypeConverter
    fun fromListToString(list: List<Int>) = gson.toJson(list)

    @TypeConverter
    fun fromStringToList(string: String): List<Int> {
        return gson.fromJson(string, object : TypeToken<List<Int>>() {}.type)
    }
}