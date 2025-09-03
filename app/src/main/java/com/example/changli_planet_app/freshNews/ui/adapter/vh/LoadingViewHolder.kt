package com.example.changli_planet_app.freshNews.ui.adapter.vh

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.databinding.LoadingViewBinding

class LoadingViewHolder(private val binding: LoadingViewBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(isLoading: Boolean) {
        with(binding) {
            if (isLoading) {
                loadingViewText.visibility = View.VISIBLE
                loadingViewProgress.visibility = View.VISIBLE
            } else {
                loadingViewText.visibility = View.GONE
                loadingViewProgress.visibility = View.GONE
            }
        }
    }
}