package com.creamaker.changli_planet_app.freshNews.data.local.mmkv

import com.creamaker.changli_planet_app.common.data.local.kv.MigratingKv

object RefreshNewsCache {
    private val kv by lazy { MigratingKv("news_local_state") }

    fun saveLikeState(newsId: Int, isLiked: Boolean) {
        kv.putBoolean("like_$newsId", isLiked)
    }

    fun getLikeState(newsId: Int): Boolean {
        return kv.getBoolean("like_$newsId", false)
    }

    fun saveLikeNum(newsId: Int, likeNum: Int) {
        kv.putInt("likeNum_$newsId", likeNum)
    }

    fun getLikeNum(newsId: Int): Int {
        return kv.getInt("likeNum_$newsId", 0)
    }

    fun saveFavoriteState(newsId: Int, isFavorited: Boolean) {
        kv.putBoolean("favorite_$newsId", isFavorited)
    }

    fun saveFavoriteNum(newsId: Int, favoriteNum: Int) {
        kv.putInt("favoriteNum_$newsId", favoriteNum)
    }

    fun getFavoriteNum(newsId: Int): Int {
        return kv.getInt("favoriteNum_$newsId", 0)
    }

    fun getFavoriteState(newsId: Int): Boolean {
        return kv.getBoolean("favorite_$newsId", false)
    }
}