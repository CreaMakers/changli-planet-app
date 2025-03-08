package com.example.changli_planet_app.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.changli_planet_app.Data.jsonbean.FoundThing
import com.example.changli_planet_app.R

class FoundThingAdapter(private val list: List<FoundThing>):RecyclerView.Adapter<FoundThingAdapter.FoundThingViewHolder>() {
    class FoundThingViewHolder(item: View):ViewHolder(item){
        var foundPicture:ImageView?=null
        val userImage: ImageView =item.findViewById(R.id.found_user_Image)
        val userName: TextView =item.findViewById(R.id.found_user_Name)
        val userFaculty: TextView =item.findViewById(R.id.found_user_Faculty)
        val foundTitle: TextView =item.findViewById(R.id.found_title)
        val foundContent: TextView =item.findViewById(R.id.found_content)
        val foundViewStub: ViewStub? =item.findViewById(R.id.found_ViewStub)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoundThingViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.found_thing,parent,false)
        return FoundThingViewHolder(view)
    }

    override fun getItemCount()=list.size

    override fun onBindViewHolder(holder: FoundThingViewHolder, position: Int) {
        setUpTextView(holder,position)
        setUpImageView(holder, position)
        holder.itemView.setOnClickListener{
            TODO()
        }
    }

    fun setUpTextView(holder: FoundThingViewHolder, position: Int){
        holder.userName.text=list[position].user_name
        holder.userFaculty.text=list[position].user_faculty
        holder.foundTitle.text=list[position].found_title
        holder.foundContent.text=list[position].found_content
    }
    fun setUpImageView(holder: FoundThingViewHolder, position: Int){
        Glide.with(holder.itemView.context)
            .load(list[position].user_image)
            .apply(RequestOptions().transform(CircleCrop()))             //头像设置成圆形的
            .into(holder.userImage)

        //加载物品图片
        if(list[position].found_thing_picture==null&&holder.foundPicture!=null){    //当前item不应加载图片，却有复用的item的图片
            holder.foundViewStub?.visibility=View.GONE
        }
        if(list[position].found_thing_picture!=null&&holder.foundPicture==null){    //当前item应加载图片，且ViewStub未inflate
            inflate(holder.foundViewStub!!,holder,position)
        }
        if(list[position].found_thing_picture!=null&&holder.foundPicture!=null){    //当前item应加载图片，且ViewStub已经inflate
            holder.foundViewStub?.visibility=View.VISIBLE
        }
    }
    fun inflate(viewStub: ViewStub,holder: FoundThingViewHolder,position: Int){
        holder.foundPicture=viewStub.inflate() as ImageView
        Glide.with(holder.itemView.context)
            .load(list[position].found_thing_picture)
            .apply(RequestOptions().centerCrop())                    //从中心填满整个ImageView
            .into(holder.foundPicture!!)
    }

}