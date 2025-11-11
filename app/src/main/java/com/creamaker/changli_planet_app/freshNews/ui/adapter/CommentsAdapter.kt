package com.creamaker.changli_planet_app.freshNews.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.creamaker.changli_planet_app.databinding.ItemCommentsBinding
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.FreshNewsItem
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.CommentsItem
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.CommentsResult
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level1CommentItem
import com.creamaker.changli_planet_app.freshNews.ui.adapter.vh.CommentsViewHolder

class CommentsAdapter(
    val context: Context,
    val onImageClick: (List<String?>,Int) -> Unit,
    val onUserClick: (Int) -> Unit,
    val onPostLevel2CommentClick: (Int, Level1CommentItem) -> Unit,
    val onLevel1CommentLikeClick: (Level1CommentItem) -> Unit,
    val onCommentResponseCountClick: (Level1CommentItem) -> Unit,
    val onPostLevel1CommentClick: () ->Unit
): RecyclerView.Adapter<CommentsViewHolder>() {
    private val commentsItem =  CommentsItem(
        freshNewsItem = null,
        level1CommentsResults = listOf(CommentsResult.Loading)
    )
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CommentsViewHolder {
        val binding =
            ItemCommentsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentsViewHolder(
            context,
            binding,
            onImageClick,
            onUserClick,
            onPostLevel2CommentClick,
            onLevel1CommentLikeClick,
            onCommentResponseCountClick,
            onPostLevel1CommentClick
        )
    }

    override fun onBindViewHolder(
        holder: CommentsViewHolder,
        position: Int
    ) {
        holder.bind(commentsItem)
    }

    override fun getItemCount(): Int {
        return 1
    }
    fun submitLevel1Comments(level1CommentsResults: List<CommentsResult>) {
        if (level1CommentsResults.isEmpty()) {
            commentsItem.level1CommentsResults = listOf(CommentsResult.Empty)
            notifyDataSetChanged()
        }
        else{
            commentsItem.level1CommentsResults =
                level1CommentsResults
            notifyDataSetChanged()
        }
    }
    fun submitFreshNews(freshNewsItem: FreshNewsItem) {
        commentsItem.freshNewsItem = freshNewsItem
        notifyDataSetChanged()
    }
}

