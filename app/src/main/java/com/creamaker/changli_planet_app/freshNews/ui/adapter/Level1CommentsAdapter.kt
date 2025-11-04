package com.creamaker.changli_planet_app.freshNews.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.creamaker.changli_planet_app.databinding.ItemCommentLevel1Binding
import com.creamaker.changli_planet_app.databinding.ItemCommentsBinding
import com.creamaker.changli_planet_app.databinding.ItemCommentsEmptyBinding
import com.creamaker.changli_planet_app.databinding.ItemCommentsErrorBinding
import com.creamaker.changli_planet_app.databinding.ItemCommentsLoadingBinding
import com.creamaker.changli_planet_app.databinding.ItemCommentsNoMoreDataBinding
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level1CommentItem
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.CommentsResult
import com.creamaker.changli_planet_app.freshNews.ui.adapter.vh.CommentsEmptyViewHolder
import com.creamaker.changli_planet_app.freshNews.ui.adapter.vh.CommentsErrorViewHolder
import com.creamaker.changli_planet_app.freshNews.ui.adapter.vh.CommentsLoadingViewHolder
import com.creamaker.changli_planet_app.freshNews.ui.adapter.vh.CommentsNoMoreViewHolder
import com.creamaker.changli_planet_app.freshNews.ui.adapter.vh.Level1CommentsViewHolder
import com.gradle.scan.agent.serialization.scan.serializer.kryo.it

class Level1CommentsAdapter(
    val context: Context,
    val onUserClick: (Int) -> Unit,
    val onPostLevel2CommentClick: (Int, Level1CommentItem) -> Unit,
    val onLevel1CommentLikeClick: (Level1CommentItem) -> Unit,
    val onCommentResponseCountClick: (Level1CommentItem) -> Unit
) : ListAdapter<CommentsResult, RecyclerView.ViewHolder>(object :
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
        return when (viewType) {
            0 -> {
                val binding = ItemCommentsLoadingBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                CommentsLoadingViewHolder(binding)
                // Loading ViewHolder
            }

            1 -> {
                // Empty ViewHolder
                val binding = ItemCommentsEmptyBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                CommentsEmptyViewHolder(binding)
            }

            2 -> {
                val binding = ItemCommentLevel1Binding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                Level1CommentsViewHolder(
                    binding,
                    onUserClick,
                    onPostLevel2CommentClick,
                    onLevel1CommentLikeClick,
                    onCommentResponseCountClick
                )
            }

            3 -> {
                val binding = ItemCommentsErrorBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                CommentsErrorViewHolder(binding)
            }

            4 -> {
                val binding = ItemCommentsNoMoreDataBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                CommentsNoMoreViewHolder(binding)
            }

            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (val item = getItem(position)) {
            is CommentsResult.Loading -> {
                // No binding needed for loading
            }

            is CommentsResult.Empty -> {
                // No binding needed for empty
            }

            is CommentsResult.Success.Level1CommentsSuccess -> {
                if (holder is Level1CommentsViewHolder) {
                    holder.bind(item.level1Comment)
                }

            }

            is CommentsResult.Error -> {
                // No binding needed for error
            }

            is CommentsResult.noMore -> {
                // No binding needed for no more data
            }

            is CommentsResult.Success.Level2CommentsSuccess -> {}
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CommentsResult.Loading -> 0
            is CommentsResult.Empty -> 1
            is CommentsResult.Success -> 2
            is CommentsResult.Error -> 3
            is CommentsResult.noMore -> 4
        }
    }


}