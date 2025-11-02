package com.creamaker.changli_planet_app.freshNews.contract

import com.creamaker.changli_planet_app.core.mvi.MviIntent
import com.creamaker.changli_planet_app.core.mvi.MviViewState
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.FreshNewsItem
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level1CommentItem
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level1CommentsResult
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level2CommentsResult

class CommentsContract {
    sealed class Intent: MviIntent{
        data class LoadFreshNews(val freshNewsItem: FreshNewsItem): Intent()
        data class SendComment(val freshNewsId:Int,val commentContent: String,val parentId: Int = -1): Intent()
        data class Level1CommentLikedClick(
            val level1CommentItem: Level1CommentItem
        ): Intent()
//        data class CommentResponseCountClick(
//            val level1CommentItem: Level1CommentItem
//        ): Intent()
        data class LoadLevel1Comments(val freshNewsItem: FreshNewsItem,val page:Int,val pageSize: Int): Intent()

    }
    data class State(
        val freshNewsItem: FreshNewsItem,
        val level1CommentsResults: List<Level1CommentsResult>,
        val level2CommentsResults:List<Level2CommentsResult>,
        val level1CommentPostState: Int = 0, //0:before_posting,1:posting,2:success,3:error
        ): MviViewState
}