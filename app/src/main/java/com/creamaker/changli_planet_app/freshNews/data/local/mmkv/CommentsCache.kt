package com.creamaker.changli_planet_app.freshNews.data.local.mmkv

import com.creamaker.changli_planet_app.common.data.local.kv.MigratingKv

object CommentsCache {
    private val kv by lazy { MigratingKv("comments_cache") }

    fun saveIp(ip: String) {
        kv.putString("ip", ip)
    }

    fun getIp(): String {
        return kv.getString("ip", "未知") ?: "未知"
    }

    fun savaLikeState(commentId: Int, isLiked: Boolean) {
        kv.putBoolean("comment_like_$commentId", isLiked)
    }

    fun getLikeState(commentId: Int): Boolean {
        return kv.getBoolean("comment_like_$commentId", false)
    }
}