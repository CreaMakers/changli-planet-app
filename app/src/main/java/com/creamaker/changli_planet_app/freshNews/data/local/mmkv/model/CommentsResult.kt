package com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model

sealed class CommentsResult{
    data object Loading: CommentsResult()
    data object Empty: CommentsResult()
    sealed class Success: CommentsResult() {
        data class Level1CommentsSuccess(val level1Comment: Level1CommentItem): Success()
        data class Level2CommentsSuccess(val level2Comment: Level2CommentItem): Success()
    }
    data object Error: CommentsResult()
    data object noMore: CommentsResult()
}
