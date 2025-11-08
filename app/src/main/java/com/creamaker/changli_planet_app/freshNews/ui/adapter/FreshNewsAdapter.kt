package com.creamaker.changli_planet_app.freshNews.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.room.util.copy
import com.creamaker.changli_planet_app.databinding.FreshNewsItemBinding
import com.creamaker.changli_planet_app.databinding.LoadingViewBinding
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.FreshNewsItem
import com.creamaker.changli_planet_app.freshNews.ui.adapter.diffUtil.NewsDiffCallback
import com.creamaker.changli_planet_app.freshNews.ui.adapter.vh.FreshNewsItemViewHolder
import com.creamaker.changli_planet_app.freshNews.ui.adapter.vh.LoadingViewHolder
import com.gradle.scan.agent.serialization.scan.serializer.kryo.it

class FreshNewsAdapter(
    val context: Context,
    private val onImageClick: (List<String?>, Int) -> Unit,
    private val onUserClick: (userId: Int) -> Unit,
    private val onMenuClick: (FreshNewsItem) -> Unit = {},
    private val onLikeClick: (Int) -> Unit = {},
    private val onCommentClick: (FreshNewsItem) -> Unit = {},
    private val onCollectClick: (FreshNewsItem) -> Unit = {}
) : ListAdapter<FreshNewsItem, RecyclerView.ViewHolder>(object :
    DiffUtil.ItemCallback<FreshNewsItem>() {
    override fun areItemsTheSame(oldItem: FreshNewsItem, newItem: FreshNewsItem): Boolean =
        oldItem.freshNewsId == newItem.freshNewsId

    override fun areContentsTheSame(oldItem: FreshNewsItem, newItem: FreshNewsItem): Boolean =
        oldItem == newItem

    override fun getChangePayload(oldItem: FreshNewsItem, newItem: FreshNewsItem): Any? {
        return when {
            oldItem.authorName != newItem.authorName || oldItem.authorAvatar != newItem.authorAvatar -> "UPDATE_AVATAR_AND_NAME"
            oldItem.liked != newItem.liked || oldItem.isLiked != newItem.isLiked -> "UPDATE_isLIKED"
            else -> null
        }
    }
})  {
    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
    }
    private val imageViewHolderPool = RecyclerView.RecycledViewPool().apply {
        setMaxRecycledViews(0, 15) // 设置最大复用数量
    }

    private var isLoading = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                val binding = FreshNewsItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                binding.imagesRecyclerView.setRecycledViewPool(imageViewHolderPool)
                FreshNewsItemViewHolder(
                    binding,
                    context,
                    onImageClick,
                    onUserClick,
                    onMenuClick,
                    onLikeClick,
                    onCommentClick,
                    onCollectClick
                )
            }

            else -> {
                val binding = LoadingViewBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                LoadingViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FreshNewsItemViewHolder && position < currentList.size){
            Log.d("FreshNewsAdapter","currentItem: ${getItem(position)}")
            holder.bind(getItem(position))
        } else if (holder is LoadingViewHolder){
            holder.bind(isLoading)
        }
    }

    override fun getItemCount(): Int = currentList.size + if (isLoading) 1 else 0

    override fun getItemViewType(position: Int): Int {
        return if (isLoading && position == currentList.size) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            val changePart = payloads[0] as String
            when (holder) {
                is FreshNewsItemViewHolder -> {
                    val item = getItem(position)
                    if (changePart == "UPDATE_AVATAR_AND_NAME") {
                        holder.updateAccountAndAvatar(item.authorName, item.authorAvatar)
                    } else if (changePart == "UPDATE_isLIKED") {
                        holder.updateIsLike(item.liked, item.isLiked)
                    }
                }

                else -> super.onBindViewHolder(holder, position, payloads)
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    fun updateData(newNewsList: List<FreshNewsItem>) {
        submitList(newNewsList)
    }

    fun addData(newItems: List<FreshNewsItem>) {
        submitList(currentList + newItems)
    }

    fun updateDataByUserId(userId: Int, newAccount: String, newAvatarUrl: String) {
        val newList = currentList.map {
            if (it.userId == userId) it.copy(authorName = newAccount, authorAvatar = newAvatarUrl) else it
        }
        submitList(newList)
    }

    private fun getCurrentPosition(item: FreshNewsItem): Int {
        return currentList.indexOfFirst { it.freshNewsId == item.freshNewsId }
    }

    fun updateIsLiked(item: FreshNewsItem, currentLikeCount: Int, isLiked: Boolean) {
        val position = getCurrentPosition(item)
        if (position == -1) return
        val newItem = currentList[position].copy(liked = currentLikeCount, isLiked = isLiked)
        val newList = currentList.toMutableList().apply { set(position, newItem) }
        submitList(newList)
    }

    fun updateItem(position: Int, item: FreshNewsItem) {
        if (position in 0 until currentList.size) {
            val newList = currentList.toMutableList()
            newList[position] = item
            submitList(newList)
        }
    }

    fun updateItemById(freshNewsId: Int, updater: (FreshNewsItem) -> FreshNewsItem) {
        val position = currentList.indexOfFirst { it.freshNewsId == freshNewsId }
        if (position != -1) {
            val newList = currentList.toMutableList()
            val oldItem = newList[position]
            val newItem = updater(oldItem)
            newList[position] = newItem
            submitList(newList)
        }
    }

    fun getData(): List<FreshNewsItem> = currentList
}