package com.creamaker.changli_planet_app.freshNews.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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
        return if (oldItem is CommentsResult.Success.Level2CommentsSuccess && newItem is CommentsResult.Success.Level2CommentsSuccess) {
            oldItem.level2Comment.commentId == newItem.level2Comment.commentId
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
        if (oldItem is CommentsResult.Success.Level2CommentsSuccess &&
            newItem is CommentsResult.Success.Level2CommentsSuccess
        ) {
            val oldComment = oldItem.level2Comment
            val newComment = newItem.level2Comment
            val changes = mutableMapOf<String, Any>()
            if (oldComment.isLiked != newComment.isLiked) {
                changes[Level1CommentsAdapter.Companion.PAYLOAD_IS_LIKED] = newComment.isLiked
            }
            if (oldComment.liked != newComment.liked) {
                changes[Level1CommentsAdapter.Companion.PAYLOAD_LIKED_COUNT] = newComment.liked
            }
            return changes.ifEmpty { null }
        }
        return null
    }

}) {

    companion object {
        const val VIEW_TYPE_LOADING = 0
        const val VIEW_TYPE_EMPTY = 1
        const val VIEW_TYPE_LEVEL1_COMMENT = 2
        const val VIEW_TYPE_LEVEL2_COMMENT = 3
        const val VIEW_TYPE_ERROR = 4
        const val VIEW_TYPE_NO_MORE = 5
        const val PAYLOAD_IS_LIKED = "isLiked" // 是否点赞 用于payload
        const val PAYLOAD_LIKED_COUNT = "liked" // 点赞数量 用于payload
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        when (viewType) {
            VIEW_TYPE_LOADING -> {
                //Loading ViewHolder
                val binding = ItemCommentsLoadingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return CommentsLoadingViewHolder(binding)
            }

            VIEW_TYPE_EMPTY -> {
                //Empty ViewHolder
                val binding = ItemCommentsEmptyBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return CommentsEmptyViewHolder(binding)
            }

            VIEW_TYPE_LEVEL1_COMMENT -> {
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

            VIEW_TYPE_LEVEL2_COMMENT -> {
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

            VIEW_TYPE_ERROR -> {
                //Error ViewHolder
                val binding = ItemCommentsErrorBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return CommentsErrorViewHolder(binding)
            }

            VIEW_TYPE_NO_MORE -> {
                //NoMore ViewHolder
                val binding = ItemCommentsNoMoreDataBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return CommentsNoMoreViewHolder(binding)
            }

            else -> {
                throw IllegalArgumentException("Invalid view type: $viewType")
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

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val item = getItem(position)
            if (holder is Level2CommentsViewHolder &&
                item is CommentsResult.Success.Level2CommentsSuccess
            ) {
                payloads.forEach { payload ->
                    if (payload is Map<*, *>) {
                        payload[Level1CommentsAdapter.Companion.PAYLOAD_IS_LIKED]?.let { isLiked ->
                            val likedCount =
                                payload[Level1CommentsAdapter.Companion.PAYLOAD_LIKED_COUNT] as? Int
                                    ?: item.level2Comment.liked
                            holder.updateLike(isLiked as Boolean, likedCount, item.level2Comment)
                        }
                    }
                }
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CommentsResult.Loading -> VIEW_TYPE_LOADING
            is CommentsResult.Empty -> VIEW_TYPE_EMPTY
            is CommentsResult.Success.Level1CommentsSuccess -> VIEW_TYPE_LEVEL1_COMMENT
            is CommentsResult.Success.Level2CommentsSuccess -> VIEW_TYPE_LEVEL2_COMMENT
            is CommentsResult.Error -> VIEW_TYPE_ERROR
            is CommentsResult.noMore -> VIEW_TYPE_NO_MORE
        }
    }

}