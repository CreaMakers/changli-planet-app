package com.creamaker.changli_planet_app.freshNews.ui.adapter

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.utils.load

class ImageAdapter(
    private var imageList: List<String?>,
    private var onImageClick: (String?, Int) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ViewModel>() {

    inner class ViewModel(val view: View) : RecyclerView.ViewHolder(view) {
        private val imageView: ImageView = view.findViewById(R.id.item_image_view)

        fun bind(imageUrl: String?, position: Int) {
            imageUrl?.let{
                Log.d("ImageAdapter", "imageUrl: $imageUrl")
                imageUrl.let {
                    imageView.load(it, listener = object : RequestListener<Drawable> {
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
                }
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

    fun updateImages(newImages: List<String?>, onClick: (String?, Int) -> Unit) {
        this.imageList = newImages
        this.onImageClick = onClick
        notifyDataSetChanged()
    }
}