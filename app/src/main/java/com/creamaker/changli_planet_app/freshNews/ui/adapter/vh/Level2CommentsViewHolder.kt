package com.creamaker.changli_planet_app.freshNews.ui.adapter.vh

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.databinding.ItemCommentLevel2Binding
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level2CommentItem
import com.creamaker.changli_planet_app.utils.load

class Level2CommentsViewHolder(
    val binding: ItemCommentLevel2Binding,
    val onResponseLevel2CommentClick: (level2CommentItem: Level2CommentItem) -> Unit,
    val onUserClick: (userId: Int) -> Unit,
    val onLevel2CommentLikedClick: (level2CommentItem: Level2CommentItem) -> Unit,
):
RecyclerView.ViewHolder(binding.root) {
    fun bind(level2CommentItem: Level2CommentItem) {
        with(binding){
            // 设置头像
            ivCommentAvatarLevel2.load(level2CommentItem.userAvatar)
            
            // 设置用户名
            tvCommentItemUsernameLevel2.text = level2CommentItem.userName
            
            // 设置时间
            commentItemTimeLevel2.text = level2CommentItem.createTime.replace("T", "   ").replace("Z", " ")
            
            // 设置位置
            if (level2CommentItem.userIp?.isNotEmpty() == true) {
                commentItemLocationLevel2.text = level2CommentItem.userIp
                dotSeparatorLevel2.visibility = if (level2CommentItem.userIp.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                commentItemLocationLevel2.visibility = if (level2CommentItem.userIp.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            } else {
                dotSeparatorLevel2.visibility = android.view.View.GONE
                commentItemLocationLevel2.visibility = android.view.View.GONE
            }
            
            // 设置评论内容
            tvCommentItemContentLevel2.text = highlightMentions(level2CommentItem.content,R.color.md_theme_onPrimaryFixedVariant)
            
            // 设置点赞数
            tvLevel2LikeCount.text = if (level2CommentItem.liked > 999) {
                "${(level2CommentItem.liked/1000f).toString()}K"
            } else {
                level2CommentItem.liked.toString()
            }
            
            // 设置点赞图标
            ivLevel2Liked.apply {
                if(level2CommentItem.isLiked){
                    load(R.drawable.ic_news_liked)
                    imageTintList = root.context.getColorStateList(R.color.ele_low)
                }
                else{
                    load(R.drawable.ic_like)
                    imageTintList = root.context.getColorStateList(R.color.md_theme_outline)
                }
                setOnClickListener {
                    onLevel2CommentLikedClick(level2CommentItem)
                }
            }
            
            // 设置点击事件
            ivCommentAvatarLevel2.setOnClickListener {
                onUserClick(level2CommentItem.userId)
            }
            tvCommentItemUsernameLevel2.setOnClickListener {
                onUserClick(level2CommentItem.userId)
            }
            tvLevel2LikeCount.setOnClickListener {
                onLevel2CommentLikedClick(level2CommentItem)
            }
            ivLevel2Liked.setOnClickListener {
                onLevel2CommentLikedClick(level2CommentItem)
            }
            tvResponseLevel2.setOnClickListener {
                onResponseLevel2CommentClick(level2CommentItem)
            }
        }
    }

    // 拉了一坨屎 嘻嘻
    fun updateLike(isLiked: Boolean, likedCount: Int, level2CommentItem: Level2CommentItem) {
        with(binding) {
            ivLevel2Liked.apply {
                if (isLiked) {
                    load(R.drawable.ic_news_liked)
                    imageTintList = root.context.getColorStateList(R.color.ele_low)
                } else {
                    load(R.drawable.ic_like)
                    imageTintList = root.context.getColorStateList(R.color.md_theme_outline)
                }
                setOnClickListener {
                    onLevel2CommentLikedClick(level2CommentItem)
                }
            }
            tvLevel2LikeCount.text = likedCount.toString()
        }
    }

    private fun highlightMentions(text: String, colorRes: Int): SpannableStringBuilder {
        val ssb = SpannableStringBuilder(text)
        val color = ContextCompat.getColor(binding.root.context, colorRes)
        val mentionRegex = Regex("@[\\p{L}0-9_]+") // 支持 Unicode 字母、数字和下划线
        mentionRegex.findAll(text).forEach { matchResult ->
            ssb.setSpan(
                ForegroundColorSpan(color),
                matchResult.range.first,
                matchResult.range.last + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return ssb
    }

}