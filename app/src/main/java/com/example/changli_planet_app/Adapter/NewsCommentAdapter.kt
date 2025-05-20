package com.example.changli_planet_app.Adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.changli_planet_app.Data.model.CommentDetailBean
import com.example.changli_planet_app.R
import com.scwang.smart.refresh.header.material.CircleImageView

class NewsCommentAdapter(
    private val context: Context,
    private var commentBeanList: List<CommentDetailBean>
) : BaseExpandableListAdapter() {

    companion object {
        private const val TAG = "CommentExpandAdapter"
    }

    override fun getGroupCount(): Int = commentBeanList.size

    override fun getChildrenCount(groupPosition: Int): Int {
        return commentBeanList[groupPosition].replyList?.size ?: 0
    }

    override fun getGroup(groupPosition: Int): Any = commentBeanList[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return commentBeanList[groupPosition].replyList!![childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long =
        childPosition.toLong()

    override fun hasStableIds(): Boolean = true

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View? {
        val holder: GroupViewHolder
        var view = convertView

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.news_comment_item, parent, false)
            holder = GroupViewHolder(view)
            view.tag = holder
        } else {
            holder = view.tag as GroupViewHolder
        }

        val groupItem = commentBeanList[groupPosition]

        Glide.with(context)
            .load(groupItem.userAvatarResId)
            .error(R.mipmap.ic_launcher)
            .centerCrop()

        holder.tvName.text = groupItem.nickName
        holder.tvTime.text = groupItem.createDate
        holder.tvContent.text = groupItem.content

        var isLiked = groupItem.isLiked
        holder.ivLike.setOnClickListener {
            isLiked = !isLiked
            groupItem.isLiked = isLiked
            holder.ivLike.setColorFilter(
                if (isLiked) Color.parseColor("#FF5C5C")
                else Color.parseColor("#AAAAAA")
            )
        }

        // 设置初始颜色
        holder.ivLike.setColorFilter(
            if (groupItem.isLiked) Color.parseColor("#FF5C5C")
            else Color.parseColor("#AAAAAA")
        )

        return view
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View? {
        val holder: ChildViewHolder
        var view = convertView

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.comment_reply_item_layout, parent, false)
            holder = ChildViewHolder(view)
            view.tag = holder
        } else {
            holder = view.tag as ChildViewHolder
        }

        val replyItem = commentBeanList[groupPosition].replyList!![childPosition]

        holder.tvName.text = "${replyItem.nickName}:"
        holder.tvContent.text = replyItem.content

        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true


    // 一级 ViewHolder
    class GroupViewHolder(view: View) {
        val tvName: TextView = view.findViewById(R.id.user_name)
        val tvContent: TextView = view.findViewById(R.id.comment_content)
        val tvTime: TextView = view.findViewById(R.id.comment_time)
        val ivLike: ImageView = view.findViewById(R.id.comment_item_like)
    }

    // 子级 ViewHolder
    class ChildViewHolder(view: View) {
        val tvName: TextView = view.findViewById(R.id.user_name)
        val tvContent: TextView = view.findViewById(R.id.comment_content)
        val tvTime: TextView = view.findViewById(R.id.comment_time)
        val ivLike: ImageView = view.findViewById(R.id.comment_item_like)
    }
}