package com.example.changli_planet_app.Adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.changli_planet_app.Core.GlideApp
import com.example.changli_planet_app.R

class ImageAdapter(
    private val imageList: List<String?>,
    private val onImageClick: (String?, Int) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ViewModel>() {

    inner class ViewModel(val view: View) : RecyclerView.ViewHolder(view) {
        private val imageView: ImageView = view.findViewById(R.id.item_image_view)

        fun bind(imageUrl: String?, position: Int) {
            imageUrl?.let{
                GlideApp.with(view)
                    .load(imageUrl)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            imageView.visibility = View.GONE
                            return true
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            imageView.setOnClickListener {
                                onImageClick(imageUrl, position)
                            }
                            return false
                        }
                    })
                    .into(imageView)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewModel {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ViewModel(view)
    }

    override fun getItemCount(): Int = imageList.size

    override fun onBindViewHolder(holder: ViewModel, position: Int) {
        holder.bind(imageList[position], position)
    }
}