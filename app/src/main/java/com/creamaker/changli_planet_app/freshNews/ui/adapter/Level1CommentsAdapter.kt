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
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level1CommentsResult
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level2CommentsResult
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
) : ListAdapter<Level1CommentsResult, RecyclerView.ViewHolder>(object :
    androidx.recyclerview.widget.DiffUtil.ItemCallback<Level1CommentsResult>() {
    override fun areItemsTheSame(
        oldItem: Level1CommentsResult,
        newItem: Level1CommentsResult
    ): Boolean {
        return if (oldItem is Level1CommentsResult.Success && newItem is Level1CommentsResult.Success) {
            oldItem.comment.commentId == newItem.comment.commentId
        } else {
            oldItem::class == newItem::class
        }
    }

    override fun areContentsTheSame(
        oldItem: Level1CommentsResult,
        newItem: Level1CommentsResult
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
            is Level1CommentsResult.Loading -> {
                // No binding needed for loading
            }

            is Level1CommentsResult.Empty -> {
                // No binding needed for empty
            }

            is Level1CommentsResult.Success -> {
                if (holder is Level1CommentsViewHolder) {
                    holder.bind(item.comment)
                }

            }

            is Level1CommentsResult.Error -> {
                // No binding needed for error
            }

            is Level1CommentsResult.noMore -> {
                // No binding needed for no more data
            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Level1CommentsResult.Loading -> 0
            is Level1CommentsResult.Empty -> 1
            is Level1CommentsResult.Success -> 2
            is Level1CommentsResult.Error -> 3
            is Level1CommentsResult.noMore -> 4
        }
    }


}