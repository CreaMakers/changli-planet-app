package com.zhelearn.CSUSTPlanet.overview.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * 公告项，对应 `GET /v1/config/announcements?platform=android` 返回数组的单个元素。
 *
 * @property id 公告唯一标识（UUID）
 * @property title 公告标题，概况页公告栏以单行标题轮播展示
 * @property content 公告正文，可能包含多行（`\n` 分段）
 * @property isBanner 服务端标记是否为横幅公告，客户端暂不区分展示
 * @property createdAt 创建时间，ISO 8601 字符串（如 `2026-06-21T16:34:00.365516+08:00`）
 */
@Keep
data class AnnouncementDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("content") val content: String = "",
    @SerializedName("isBanner") val isBanner: Boolean = false,
    @SerializedName("createdAt") val createdAt: String = ""
)
