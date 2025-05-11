package com.example.changli_planet_app.Adapter

import android.graphics.Bitmap
import android.util.LruCache
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Widget.View.OnAlreadyListener
import com.example.changli_planet_app.databinding.ItemRenderPdfBinding


class PdfRenderAdapter:RecyclerView.Adapter<PdfRenderAdapter.PdfHolder>() {
    inner class PdfHolder(val binding: ItemRenderPdfBinding):RecyclerView.ViewHolder(binding.root)

    private val maxMemory=(Runtime.getRuntime().maxMemory()/1024).toInt()
    private val cache=LruCache<Int,Bitmap>(maxMemory/8)

    public fun add(position: Int,bitmap: Bitmap){
        cache.put(position,bitmap)
        notifyItemInserted(position)
    }

    public fun getCache()=cache

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfHolder {
        val binding=ItemRenderPdfBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PdfHolder(binding)
    }

    override fun getItemCount(): Int =cache.putCount()

    override fun onBindViewHolder(holder: PdfHolder, position: Int) {
        val adapterPosition=holder.adapterPosition
        holder.binding.pdfView.setOnAlreadyListener(object :OnAlreadyListener{
            override fun onAlready(holder: SurfaceHolder): Bitmap {
                return cache.get(adapterPosition)
            }

            override fun onFinish(holder: SurfaceHolder) {
            }

        })
    }
}