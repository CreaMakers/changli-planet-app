package com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model

sealed class Level1CommentsResult{
    data object Loading: Level1CommentsResult()
    data object Empty: Level1CommentsResult()
    data class Success(val comment: Level1CommentItem): Level1CommentsResult()
    data object Error: Level1CommentsResult()
    data object noMore: Level1CommentsResult()
}
