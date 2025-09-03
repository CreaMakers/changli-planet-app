package com.example.changli_planet_app.Feature.lostfound.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.changli_planet_app.Data.jsonbean.LoseThing
import com.example.changli_planet_app.R

class LoseThingAdapter(private val list: List<LoseThing>):RecyclerView.Adapter<LoseThingAdapter.LoseThingsViewHolder>() {

    class LoseThingsViewHolder(item:View): ViewHolder(item){
        var losePicture:ImageView?=null
        val userImage:ImageView=item.findViewById(R.id.lose_user_Image)
        val userName:TextView=item.findViewById(R.id.lose_user_Name)
        val userFaculty:TextView=item.findViewById(R.id.lose_user_Faculty)
        val loseTitle:TextView=item.findViewById(R.id.lose_title)
        val loseContent:TextView=item.findViewById(R.id.lose_content)
        val loseViewStub:ViewStub?=item.findViewById(R.id.lose_ViewStub)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoseThingsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lose_thing,parent,false)
        return LoseThingsViewHolder(view)
    }
    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: LoseThingsViewHolder, position: Int) {
        setUpTextView(holder,position)
        setUpImageView(holder,position)
        holder.itemView.setOnClickListener{
            Toast.makeText(holder.itemView.context,"clickItem",Toast.LENGTH_SHORT).show()
            //TODO
        }
    }

    fun setUpTextView(holder: LoseThingsViewHolder, position: Int){
        holder.userName.text=list[position].user_name
        holder.userFaculty.text=list[position].user_faculty
        holder.loseTitle.text=list[position].lose_title
        holder.loseContent.text=list[position].lose_content
    }
    fun setUpImageView(holder: LoseThingsViewHolder, position: Int){
        Glide.with(holder.itemView.context)
            .load(list[position].user_image)
            .apply(RequestOptions().transform(CircleCrop()))             //头像设置成圆形的
            .into(holder.userImage)

        //加载物品图片
        if(list[position].lose_thing_picture==null&&holder.losePicture!=null){    //当前item不应加载图片，却有复用的item的图片
            holder.loseViewStub?.visibility=View.GONE
        }
        if(list[position].lose_thing_picture!=null&&holder.losePicture==null){    //当前item应加载图片，且ViewStub未inflate
            inflate(holder.loseViewStub!!,holder,position)                 //使用ViewStub懒加载图片
        }
        if(list[position].lose_thing_picture!=null&&holder.losePicture!=null){    //当前item应加载图片，且ViewStub已经inflate
            holder.loseViewStub?.visibility=View.VISIBLE
        }
    }
    fun inflate(viewStub:ViewStub,holder:LoseThingsViewHolder,position: Int){
         holder.losePicture=viewStub.inflate() as ImageView
         Glide.with(holder.itemView.context)
             .load(list[position].lose_thing_picture)
             .apply(RequestOptions().centerCrop())                    //从中心填满整个ImageView
             .into(holder.losePicture!!)
        //Log.d("loseThingAdapter","picture position"+position)
    }
}