package com.creamaker.changli_planet_app.freshNews.ui.adapter.vh

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.databinding.ItemCommentsBinding
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.CommentsItem
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level1CommentItem
import com.creamaker.changli_planet_app.freshNews.ui.adapter.ImageAdapter
import com.creamaker.changli_planet_app.freshNews.ui.adapter.Level1CommentsAdapter
import com.creamaker.changli_planet_app.utils.load

//import com.creamaker.changli_planet_app.freshNews.ui.adapter.Level1CommentsAdapter

class CommentsViewHolder(
    val context: Context,
    val binding: ItemCommentsBinding,
    val onImageClick: (List<String?>, Int) -> Unit,
    val onUserClick: (Int) -> Unit,
    val onPostLevel2CommentClick: (Int, Level1CommentItem) -> Unit,
    val onLevel1CommentLikeClick: (Level1CommentItem) -> Unit,
    val onCommentResponseCountClick: (Level1CommentItem) -> Unit,
    val onPostLevel1CommentClick: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val level1CommentsAdapter = Level1CommentsAdapter(
        context,
        onUserClick,
        onPostLevel2CommentClick,
        onLevel1CommentLikeClick,
        onCommentResponseCountClick
    )

    private val imageAdapter = ImageAdapter(emptyList()) { _, _ -> }

    init {
        with(binding) {
            imagesRecyclerView.apply {
                adapter = imageAdapter
                isNestedScrollingEnabled = false
            }

            rvComments.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = level1CommentsAdapter
                isNestedScrollingEnabled = false
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun bind(commentsItem: CommentsItem) = with(binding) {
        val context = root.context
        val fresh = commentsItem.freshNewsItem
        val commentsCount = commentsItem.level1CommentsResults.size-1
        tvCommentsCount.text = context.getString(R.string.comments_num, commentsCount)
        // 设置图片 RecyclerView
        val images = fresh?.images ?: emptyList()
        (imagesRecyclerView.adapter as? ImageAdapter)?.updateImages(images) { _, position ->
            onImageClick(images, position)
        }
        level1CommentsAdapter.submitList(commentsItem.level1CommentsResults)

        if (fresh != null) {
            // 新鲜事绑定
            postmanAvatar.load(fresh.authorAvatar)
            postmanUsername.text = fresh.authorName
            postmanTime.text = fresh.createTime.replace("T", "   ").replace("Z", " ")
            postmanLocation.text = fresh.location
            newsTitle.text = fresh.title
            newsContent.text = fresh.content
            tvCommentsCount.text = "(${fresh.comments})"
            postmanAvatar.setOnClickListener { onUserClick(fresh.userId) }
            tvPostComment.setOnClickListener { onPostLevel1CommentClick() }

        } else {
            // 占位内容（加载中状态）
            postmanAvatar.load("https://pic.imgdb.cn/item/671e5e17d29ded1a8c5e0dbe.jpg")
            with(context) {
                postmanTime.text = getString(R.string.on_process)
                postmanLocation.text = getString(R.string.on_process)
                newsTitle.text = getString(R.string.on_process)
                newsContent.text = getString(R.string.on_process)
            }
        }
    }

}