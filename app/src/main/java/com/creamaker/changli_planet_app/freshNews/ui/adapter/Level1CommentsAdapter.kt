package com.creamaker.changli_planet_app.freshNews.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.creamaker.changli_planet_app.databinding.ItemCommentLevel1Binding
import com.creamaker.changli_planet_app.databinding.ItemCommentsEmptyBinding
import com.creamaker.changli_planet_app.databinding.ItemCommentsErrorBinding
import com.creamaker.changli_planet_app.databinding.ItemCommentsLoadingBinding
import com.creamaker.changli_planet_app.databinding.ItemCommentsNoMoreDataBinding
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.CommentsResult
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level1CommentItem
import com.creamaker.changli_planet_app.freshNews.ui.adapter.vh.CommentsEmptyViewHolder
import com.creamaker.changli_planet_app.freshNews.ui.adapter.vh.CommentsErrorViewHolder
import com.creamaker.changli_planet_app.freshNews.ui.adapter.vh.CommentsLoadingViewHolder
import com.creamaker.changli_planet_app.freshNews.ui.adapter.vh.CommentsNoMoreViewHolder
import com.creamaker.changli_planet_app.freshNews.ui.adapter.vh.Level1CommentsViewHolder

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

    override fun getChangePayload(
        oldItem: CommentsResult,
        newItem: CommentsResult
    ): Any? {
        if (oldItem is CommentsResult.Success.Level1CommentsSuccess &&
            newItem is CommentsResult.Success.Level1CommentsSuccess
        ) {
            val oldComment = oldItem.level1Comment
            val newComment = newItem.level1Comment
            val changes = mutableMapOf<String, Any>()
            if (oldComment.isLiked != newComment.isLiked) {
                changes[PAYLOAD_IS_LIKED] = newComment.isLiked
            }
            if (oldComment.liked != newComment.liked) {
                changes[PAYLOAD_LIKED_COUNT] = newComment.liked
            }
            return changes.ifEmpty { null }
        }
        return null
    }
}) {

    companion object {
        const val STATE_LOADING = 0
        const val STATE_EMPTY = 1
        const val STATE_SUCCESS = 2
        const val STATE_ERROR = 3
        const val STATE_NO_MORE = 4
        const val PAYLOAD_IS_LIKED = "isLiked" // 是否点赞 用于payload
        const val PAYLOAD_LIKED_COUNT = "liked" // 点赞数量 用于payload
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            STATE_LOADING -> {
                val binding = ItemCommentsLoadingBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                CommentsLoadingViewHolder(binding)
                // Loading ViewHolder
            }

            STATE_EMPTY -> {
                // Empty ViewHolder
                val binding = ItemCommentsEmptyBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                CommentsEmptyViewHolder(binding)
            }

            STATE_SUCCESS -> {
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

            STATE_ERROR -> {
                val binding = ItemCommentsErrorBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                CommentsErrorViewHolder(binding)
            }

            STATE_NO_MORE -> {
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
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val item = getItem(position)
            if (holder is Level1CommentsViewHolder &&
                item is CommentsResult.Success.Level1CommentsSuccess
            ) {

                payloads.forEach { payload ->
                    if (payload is Map<*, *>) {
                        payload[PAYLOAD_IS_LIKED]?.let { isLiked ->
                            val likedCount = payload[PAYLOAD_LIKED_COUNT] as? Int
                                ?: item.level1Comment.liked
                            holder.updateLike(isLiked as Boolean, likedCount, item.level1Comment)
                        }
                    }
                }
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
            is CommentsResult.Loading -> STATE_LOADING
            is CommentsResult.Empty -> STATE_EMPTY
            is CommentsResult.Success -> STATE_SUCCESS
            is CommentsResult.Error -> STATE_ERROR
            is CommentsResult.noMore -> STATE_NO_MORE
        }
    }


}