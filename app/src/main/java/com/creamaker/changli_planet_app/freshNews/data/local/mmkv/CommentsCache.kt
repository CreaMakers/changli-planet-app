package com.creamaker.changli_planet_app.freshNews.data.local.mmkv

object CommentsCache {
    private val mmkv by lazy {
        com.tencent.mmkv.MMKV.mmkvWithID("comments_cache")
    }

    fun saveIp(ip: String) {
        mmkv.encode("ip", ip)
    }
    fun getIp(): String {
        return mmkv.decodeString("ip", "未知") ?: "未知"
    }
    fun savaLikeState(commentId: Int, isLiked: Boolean) {
        mmkv.encode("comment_like_$commentId", isLiked)
    }
    fun getLikeState(commentId: Int): Boolean {
        return mmkv.decodeBool("comment_like_$commentId", false)
    }
}