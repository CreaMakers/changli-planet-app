package com.creamaker.changli_planet_app.freshNews.data.local.mmkv

import com.tencent.mmkv.MMKV

object RefreshNewsCache {
    private val mmkv by lazy { MMKV.mmkvWithID("news_local_state") }

    fun saveLikeState(newsId: Int, isLiked: Boolean) {
        mmkv?.encode("like_$newsId", isLiked)
    }

    fun getLikeState(newsId: Int): Boolean {
        return mmkv?.decodeBool("like_$newsId", false) ?: false
    }

    fun saveLikeNum(newsId: Int, LikeNum: Int) {
        mmkv?.encode("likeNum_$newsId", LikeNum)
    }

    fun getLikeNum(newsId: Int): Int {
        return mmkv?.decodeInt("likeNum_$newsId", 0) ?: 0
    }

    fun saveFavoriteState(newsId: Int, isFavorited: Boolean) {
        mmkv?.encode("favorite_$newsId", isFavorited)
    }

    fun saveFavoriteNum(newsId: Int, FavoriteNum: Int) {
        mmkv?.encode("favoriteNum_$newsId", FavoriteNum)
    }

    fun getFavoriteNum(newsId: Int): Int {
        return mmkv?.decodeInt("favoriteNum_$newsId", 0) ?: 0
    }

    fun getFavoriteState(newsId: Int): Boolean {
        return mmkv?.decodeBool("favorite_$newsId", false) ?: false
    }
}