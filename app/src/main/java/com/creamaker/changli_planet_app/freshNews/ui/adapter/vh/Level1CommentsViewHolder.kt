package com.creamaker.changli_planet_app.freshNews.ui.adapter.vh

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.databinding.ItemCommentLevel1Binding
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level1CommentItem
import com.creamaker.changli_planet_app.utils.load

class Level1CommentsViewHolder(
    val binding: ItemCommentLevel1Binding,
    val onUserClick: (Int) -> Unit,
    val onPostLevel2CommentClick: (Int, Level1CommentItem) -> Unit,
    val onLevel1CommentLikeClick: (Level1CommentItem) -> Unit,
    val onCommentResponseCountClick: (Level1CommentItem) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(level1CommentItem: Level1CommentItem) {
        with(binding) {
            ivCommentAvatar.apply {
                load(level1CommentItem.userAvatar)
                setOnClickListener {
                    onUserClick(level1CommentItem.userId)
                }
            }
            tvCommentItemUsername.text = level1CommentItem.userName
            tvCommentItemTime.text = level1CommentItem.createTime.replace("T", "   ").replace("Z", " ")
            tvCommentItemLocation.text = level1CommentItem.userIp
            tvCommentItemContent.text = level1CommentItem.content
            if (level1CommentItem.level2CommentsCount == 0){
                tvCommentResponseCount.visibility = android.view.View.INVISIBLE
            } else {
                tvCommentResponseCount.visibility = android.view.View.VISIBLE
            }
            tvCommentResponseCount.text = "共${level1CommentItem.level2CommentsCount}条回复  >"
            tvCommentResponseCount.setOnClickListener {
                onCommentResponseCountClick(level1CommentItem)
            }
            ivLevel1Liked.apply {
                    if(level1CommentItem.isLiked){
                        load(R.drawable.ic_news_liked)
                        imageTintList = root.context.getColorStateList(R.color.ele_low)
                    }
                    else{
                        load(R.drawable.ic_like)
                        imageTintList = root.context.getColorStateList(R.color.md_theme_outline)
                    }
                setOnClickListener {
                    onLevel1CommentLikeClick(level1CommentItem)
                }
            }

            tvLevel1LikeCount.text = level1CommentItem.liked.toString()
            tvResponse.setOnClickListener {
                onPostLevel2CommentClick(level1CommentItem.commentId, level1CommentItem)
            }
        }
    }

}