package com.creamaker.changli_planet_app.freshNews.ui.adapter

import android.content.Context
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level2CommentItem


class Level2CommentsAdapter(
    val context: Context,
    val onResponseLevel2CommentClick: (level2CommentItem: Level2CommentItem) -> Unit,
    val onLikedClick: (level2CommentItem: Level2CommentItem) -> Unit
) {
}