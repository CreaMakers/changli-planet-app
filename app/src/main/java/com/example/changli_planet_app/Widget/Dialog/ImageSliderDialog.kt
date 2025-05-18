package com.example.changli_planet_app.Widget.Dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Utils.GlideUtils
import com.github.chrisbanes.photoview.PhotoView

class ImageSliderDialog(
    context: Context,
    private val imageList: List<String?>,
    private val initialPosition: Int = 0
) : Dialog(context, R.style.CustomDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_image_slider)

        // 设置全屏
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        // 初始化ViewPager2
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = ImageSliderAdapter(imageList)
        viewPager.currentItem = initialPosition

        // 添加指示器(可选)
        val indicator = findViewById<TextView>(R.id.tvIndicator)
        if (imageList.size > 1) {
            indicator?.visibility = View.VISIBLE
            indicator?.text = "${initialPosition + 1}/${imageList.size}"

            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    indicator?.text = "${position + 1}/${imageList.size}"
                }
            })
        } else {
            indicator?.visibility = View.GONE
        }

        // 添加关闭按钮
        findViewById<View>(R.id.btnClose)?.setOnClickListener {
            dismiss()
        }
    }

    // 图片滑动适配器
    private inner class ImageSliderAdapter(private val images: List<String?>) :
        RecyclerView.Adapter<ImageSliderAdapter.PhotoViewHolder>() {

        inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val photoView: PhotoView = itemView.findViewById(R.id.photoView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_photo_view, parent, false)
            return PhotoViewHolder(view)
        }

        override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
            val imageUrl = images[position]
            if (imageUrl != null) {
                GlideUtils.load(context, holder.photoView, imageUrl, true)

                // 可选: 单击隐藏/显示UI
                holder.photoView.setOnClickListener {
                    toggleUiVisibility()
                }
            }
        }

        override fun getItemCount(): Int = images.size
    }

    private fun toggleUiVisibility() {
        val btnClose = findViewById<View>(R.id.btnClose)
        val indicator = findViewById<View>(R.id.tvIndicator)

        if (btnClose?.visibility == View.VISIBLE) {
            btnClose.visibility = View.GONE
            indicator?.visibility = View.GONE
        } else {
            btnClose?.visibility = View.VISIBLE
            if (imageList.size > 1) {
                indicator?.visibility = View.VISIBLE
            }
        }
    }
}