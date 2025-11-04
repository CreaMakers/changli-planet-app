package com.creamaker.changli_planet_app.freshNews.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.creamaker.changli_planet_app.common.data.local.mmkv.UserInfoManager.userAvatar
import com.creamaker.changli_planet_app.common.data.local.mmkv.UserInfoManager.userId
import com.creamaker.changli_planet_app.databinding.ItemCommentLevel1Binding
import com.creamaker.changli_planet_app.databinding.ItemCommentLevel2Binding
import com.creamaker.changli_planet_app.databinding.ItemCommentsEmptyBinding
import com.creamaker.changli_planet_app.databinding.ItemCommentsErrorBinding
import com.creamaker.changli_planet_app.databinding.ItemCommentsLoadingBinding
import com.creamaker.changli_planet_app.databinding.ItemCommentsNoMoreDataBinding
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.CommentsResult
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level1CommentItem
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level2CommentItem
import com.creamaker.changli_planet_app.freshNews.ui.adapter.vh.CommentsEmptyViewHolder
import com.creamaker.changli_planet_app.freshNews.ui.adapter.vh.CommentsErrorViewHolder
import com.creamaker.changli_planet_app.freshNews.ui.adapter.vh.CommentsLoadingViewHolder
import com.creamaker.changli_planet_app.freshNews.ui.adapter.vh.CommentsNoMoreViewHolder
import com.creamaker.changli_planet_app.freshNews.ui.adapter.vh.Level1CommentsViewHolder
import com.creamaker.changli_planet_app.freshNews.ui.adapter.vh.Level2CommentsViewHolder
import com.gradle.scan.plugin.internal.dep.com.fasterxml.jackson.annotation.JsonAutoDetect
import kotlin.Int


class Level2CommentsAdapter(
    val context: Context,
    val onResponseLevel2CommentClick: (level2CommentItem: Level2CommentItem) -> Unit,
    val onUserClick: (userId: Int) -> Unit,
    val onLevel1CommentLikedClick: (level1CommentItem: Level1CommentItem) -> Unit,
    val onLevel2CommentLikedClick: (level2CommentItem: Level2CommentItem) -> Unit,
): ListAdapter<CommentsResult, RecyclerView.ViewHolder>(object :
    androidx.recyclerview.widget.DiffUtil.ItemCallback<CommentsResult>() {
    override fun areItemsTheSame(
        oldItem: CommentsResult,
        newItem: CommentsResult
    ): Boolean {
        return if (oldItem is CommentsResult.Success.Level1CommentsSuccess && newItem is CommentsResult.Success.Level1CommentsSuccess) {
            oldItem.level1Comment.commentId == newItem.level1Comment.commentId
        } else {
            oldItem::class == newItem::class
        }
    }

    override fun areContentsTheSame(
        oldItem: CommentsResult,
        newItem: CommentsResult
    ): Boolean {
        return oldItem == newItem
    }
}) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        when(viewType){
            0->{
                //Loading ViewHolder
                val binding = ItemCommentsLoadingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return CommentsLoadingViewHolder(binding)
            }
            1->{
                //Empty ViewHolder
                val binding = ItemCommentsEmptyBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return CommentsEmptyViewHolder(binding)
            }
            2->{
                //Level1Comments ViewHolder
                val binding = ItemCommentLevel1Binding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return Level1CommentsViewHolder(
                    binding,
                    onUserClick,
                    {_,_->},
                    onLevel1CommentLikedClick,
                    {_->}
                    )
            }
            3->{
                //Level2Comments ViewHolder
                val binding = ItemCommentLevel2Binding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return Level2CommentsViewHolder(
                    binding,
                    onResponseLevel2CommentClick,
                    onUserClick,
                    onLevel2CommentLikedClick
                    )
            }
            4->{
                //Error ViewHolder
                val binding = ItemCommentsErrorBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return CommentsErrorViewHolder(binding)
            }
            5->{
                //NoMore ViewHolder
                val binding = ItemCommentsNoMoreDataBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return CommentsNoMoreViewHolder(binding)
            }
            else->{
                throw IllegalArgumentException("Invalid view type")
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        when (item) {
            is CommentsResult.Loading -> {
                // No binding needed for loading
            }
            is CommentsResult.Empty -> {
                // No binding needed for empty
            }
            is CommentsResult.Error -> {
                // No binding needed for error
            }

            is CommentsResult.Success.Level1CommentsSuccess -> {
                if (holder is Level1CommentsViewHolder) {
                    holder.bind(item.level1Comment)
                    with(holder.binding){
                        tvResponse.visibility = View.INVISIBLE
                        tvCommentResponseCount.visibility = View.INVISIBLE
                    }
                }
            }
            is CommentsResult.Success.Level2CommentsSuccess -> {
                if (holder is Level2CommentsViewHolder) {
                    holder.bind(item.level2Comment)
                }
            }
            CommentsResult.noMore -> {}
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CommentsResult.Loading -> 0
            is CommentsResult.Empty -> 1
            is CommentsResult.Success.Level1CommentsSuccess -> 2
            is CommentsResult.Success.Level2CommentsSuccess -> 3
            is CommentsResult.Error -> 4
            is CommentsResult.noMore -> 5
        }
    }

}