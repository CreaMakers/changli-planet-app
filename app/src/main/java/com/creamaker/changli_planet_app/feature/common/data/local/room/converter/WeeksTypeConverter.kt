package com.creamaker.changli_planet_app.feature.common.data.local.room.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 把 `weeks: List<Int>` 在 Room 里持久化成 JSON 字符串。
 *
 * 解析侧必须 **容错**：若 DB 里残留了老版本的非法 JSON（例如曾经写入过根节点是对象的字符串，
 * 或字段损坏），`Gson.fromJson` 会抛 `JsonSyntaxException` 导致 Room 查询线程崩溃。
 * 这里 `runCatching` 兜底返回空列表，让上层拿到一个\"空周次\"的安全值。
 */
class WeeksTypeConverter {
    private val gson = Gson()
    private val listType = object : TypeToken<List<Int>>() {}.type

    @TypeConverter
    fun fromListToString(list: List<Int>): String? = gson.toJson(list)

    @TypeConverter
    fun fromStringToList(string: String): List<Int> {
        if (string.isBlank()) return emptyList()
        return runCatching {
            gson.fromJson<List<Int>>(string, listType).orEmpty()
        }.getOrDefault(emptyList())
    }
}