package com.example.changli_planet_app.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Adapter.DiffUtils.NewsDiffCallback
import com.example.changli_planet_app.Adapter.ViewHolder.FreshNewsItemViewModel
import com.example.changli_planet_app.Adapter.ViewHolder.LoadingViewHolder
import com.example.changli_planet_app.Network.Response.FreshNewsItem
import com.example.changli_planet_app.databinding.FreshNewsItemBinding
import com.example.changli_planet_app.databinding.LoadingViewBinding

class FreshNewsAdapter(
    val context: Context,
    private val onImageClick: (String) -> Unit,
    private val onNewsClick: () -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
                FreshNewsItemViewModel(binding, context, onImageClick, onNewsClick)
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
            is FreshNewsItemViewModel -> {
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

}