package com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model

sealed class Level2CommentsResult {
    data object Loading: Level2CommentsResult()
    data object Empty: Level2CommentsResult()
    data class Success(val comment: Level2Comments): Level2CommentsResult()
    data object Error: Level2CommentsResult()
    data object noMore: Level2CommentsResult()
}