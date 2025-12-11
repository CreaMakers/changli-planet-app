package com.dcelysia.csust_spider.core

import android.util.Log
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import okhttp3.logging.HttpLoggingInterceptor

object NetworkLogger {

    private const val TAG = "NetworkLogger"

    /**
     * 返回一个用于日志记录的 HttpLoggingInterceptor，专门处理请求和响应日志。
     */
    fun getLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            // 如果消息是 JSON 格式，进行格式化处理
            if (message.startsWith("{") || message.startsWith("[")) {
                try {
                    val jsonElement = JsonParser.parseString(message)
                    val formattedJson = formatJson(jsonElement)
                    Log.d(TAG, formattedJson)  // 打印格式化后的 JSON
                } catch (e: Exception) {
                    Log.d(TAG, message)  // 如果格式化失败，直接打印原始信息
                }
            } else {
                Log.d(TAG, message)  // 打印非 JSON 的日志
            }
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY  // 设置日志级别为 BODY，打印请求/响应体
        }
    }

    /**
     * 格式化 JSON，使每个属性都单独一行
     * @param jsonElement JSON 数据
     * @return 格式化后的 JSON 字符串
     */
    private fun formatJson(jsonElement: JsonElement): String {
        val formattedJson = StringBuilder()

        when {
            jsonElement.isJsonObject -> {
                val jsonObject = jsonElement.asJsonObject
                formattedJson.append("{\n")
                jsonObject.keySet().forEach { key ->
                    formattedJson.append("  \"$key\": ${formatJsonValue(jsonObject[key])},\n")
                }

                // 移除最后的逗号
                if (formattedJson.length > 2) {
                    formattedJson.delete(formattedJson.length - 2, formattedJson.length)
                }

                formattedJson.append("\n}")
            }
            jsonElement.isJsonArray -> {
                formattedJson.append("[\n")
                jsonElement.asJsonArray.forEach {
                    formattedJson.append("  ${formatJsonValue(it)},\n")
                }
                // 移除最后的逗号
                if (formattedJson.length > 2) {
                    formattedJson.delete(formattedJson.length - 2, formattedJson.length)
                }
                formattedJson.append("\n]")
            }
            else -> {
                formattedJson.append(formatJsonValue(jsonElement))
            }
        }

        return formattedJson.toString()
    }

    /**
     * 格式化 JSON 的值
     * @param jsonElement JSON 值
     * @return 格式化后的值
     */
    private fun formatJsonValue(jsonElement: JsonElement): String {
        return when {
            jsonElement.isJsonPrimitive -> "\"${jsonElement.asString}\""
            jsonElement.isJsonNull -> "null"
            else -> jsonElement.toString()
        }
    }
}