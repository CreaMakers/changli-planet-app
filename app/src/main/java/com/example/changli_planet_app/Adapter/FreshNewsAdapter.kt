package com.example.changli_planet_app.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Adapter.DiffUtils.NewsDiffCallback
import com.example.changli_planet_app.Adapter.ViewHolder.FreshNewsItemViewHolder
import com.example.changli_planet_app.Adapter.ViewHolder.LoadingViewHolder
import com.example.changli_planet_app.Network.Response.FreshNewsItem
import com.example.changli_planet_app.databinding.FreshNewsItemBinding
import com.example.changli_planet_app.databinding.LoadingViewBinding

class FreshNewsAdapter(
    val context: Context,
    private val onImageClick: (List<String?>, Int) -> Unit,
    private val onUserClick: (userId: Int) -> Unit,
    private val onMenuClick: (FreshNewsItem) -> Unit = {},
    private val onLikeClick: (FreshNewsItem) -> Unit = {},
    private val onCommentClick: (FreshNewsItem) -> Unit = {},
    private val onCollectClick: (FreshNewsItem) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
    }

    private val newsList = mutableListOf<FreshNewsItem>()
    private var isLoading = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                val binding = FreshNewsItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
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

    override fun getItemCount(): Int = newsList.size + if (isLoading) 1 else 0

    override fun getItemViewType(position: Int): Int {
        return if (position == newsList.size) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FreshNewsItemViewHolder -> {
                holder.bind(newsList[position])
            }

            is LoadingViewHolder -> {
                holder.bind(isLoading)
            }
        }
    }

    fun updateData(newNewsList: List<FreshNewsItem>) {
        val diffResult = DiffUtil.calculateDiff(NewsDiffCallback(newsList, newNewsList), false)
        newsList.clear()
        newsList.addAll(newNewsList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun addData(newItems: List<FreshNewsItem>) {
        val startPosition = newsList.size
        newsList.addAll(newItems)
        notifyItemRangeInserted(startPosition, newItems.size)
    }

    fun setLoading(loading: Boolean) {
        isLoading = loading
        notifyItemChanged(itemCount - 1)
    }

    // 获取指定FreshNewsItem的位置
    fun getCurrentPosition(item: FreshNewsItem): Int {
        return newsList.indexOfFirst { it.freshNewsId == item.freshNewsId }
    }

    // 更新指定位置的项目
    fun updateItem(position: Int, item: FreshNewsItem) {
        if (position in 0 until newsList.size) {
            newsList[position] = item
            notifyItemChanged(position)
        }
    }

    // 可选：直接通过ID更新项目
    fun updateItemById(freshNewsId: Int, updater: (FreshNewsItem) -> FreshNewsItem) {
        val position = newsList.indexOfFirst { it.freshNewsId == freshNewsId }
        if (position != -1) {
            val oldItem = newsList[position]
            val newItem = updater(oldItem)
            newsList[position] = newItem
            notifyItemChanged(position)
        }
    }

    // 获取当前数据列表（只读）
    fun getData(): List<FreshNewsItem> = newsList.toList()
}