package com.example.changli_planet_app.profileSettings.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.databinding.ItemSettingHeaderBinding
import com.example.changli_planet_app.databinding.ItemSettingOptionBinding
import com.example.changli_planet_app.profileSettings.ui.adapter.model.SettingItem
import com.example.changli_planet_app.utils.load

class SettingAdapter(
    private val settingItems: List<SettingItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onSettingItemClickListener: OnSettingItemClickListener? = null

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_OPTION = 1
    }

    fun setOnSettingItemClickListener(listener: OnSettingItemClickListener) {
        this.onSettingItemClickListener = listener
    }

    override fun getItemViewType(position: Int): Int {
        return if (settingItems[position].isHeader) TYPE_HEADER else TYPE_OPTION
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemSettingHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemSettingOptionBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                OptionViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = settingItems[position]
        when (holder) {
            is HeaderViewHolder -> holder.bind(item)
            is OptionViewHolder -> holder.bind(item)
        }
    }

    override fun getItemCount(): Int = settingItems.size

    class HeaderViewHolder(
        private val binding: ItemSettingHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SettingItem) {
            binding.tvHeaderTitle.text = item.title
        }
    }

    inner class OptionViewHolder(
        private val binding: ItemSettingOptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSettingItemClickListener?.onSettingItemClick(settingItems[position])
                }
            }
        }

        fun bind(item: SettingItem) {
            with(binding) {
                tvSettingTitle.text = item.title
                if (item.iconResId != 0) {
                    ivSettingIcon.load(item.iconResId)
                    ivSettingIcon.visibility = View.VISIBLE
                } else {
                    ivSettingIcon.visibility = View.GONE
                }
            }
        }
    }

    fun interface OnSettingItemClickListener {
        fun onSettingItemClick(item: SettingItem)
    }
}