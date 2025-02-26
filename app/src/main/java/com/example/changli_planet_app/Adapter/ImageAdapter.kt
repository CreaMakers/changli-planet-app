package com.example.changli_planet_app.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Util.GlideUtils

class ImageAdapter(
    private val imageList: List<String>,
    private val onImageClick: (String) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ViewModel>() {

    inner class ViewModel(val view: View) : RecyclerView.ViewHolder(view) {
        private val imageView: ImageView = view.findViewById(R.id.item_image_view)

        fun bind(imageUrl: String) {
            GlideUtils.load(
                view,
                imageView,
                imageUrl
            )
            imageView.setOnClickListener { onImageClick(imageUrl) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewModel {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ViewModel(view)
    }

    override fun getItemCount(): Int = imageList.size

    override fun onBindViewHolder(holder: ViewModel, position: Int) {
        holder.bind(imageList[position])
    }

}