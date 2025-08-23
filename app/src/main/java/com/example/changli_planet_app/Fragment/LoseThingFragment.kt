package com.example.changli_planet_app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.changli_planet_app.Adapter.LoseThingAdapter
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.Data.jsonbean.LoseThing
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.FragmentLoseThingBinding
import kotlin.concurrent.thread

class LoseThingFragment:Fragment() {                                   //失物招领的fragment
    private lateinit var binding: FragmentLoseThingBinding
    private lateinit var loseAdapter:LoseThingAdapter
    private lateinit var item:List<LoseThing>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initListTest()
        binding = FragmentLoseThingBinding.inflate(layoutInflater)

        loseAdapter=LoseThingAdapter(item)
        binding.loseRecyclerView.layoutManager=LinearLayoutManager(context)
        binding.loseRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))    //设置item之间的分割线
        binding.loseRecyclerView.adapter=loseAdapter
        setUpImageView()                                        //动态添加图片
        setOnImageViewListener()                                //添加addLoseThing的点击事件

        binding.swipeRefresh.setColorSchemeResources(R.color.black)
        binding.swipeRefresh.setOnRefreshListener {
            refresh()
        }
        return binding.root
    }

    private fun initListTest(){
        val longText="nya~,一个甘城猫，在12教210教室不见了www一个甘城猫，在12教210教室不见了www一个甘城猫，在12教210教室不见了www一个甘城猫，在12教210教室不见了www一个甘城猫，在12教210教室不见了www一个甘城猫，在12教210教室不见了www一个甘城猫，在12教210教室不见了www一个甘城猫，在12教210教室不见了www"
    }
    fun setItem(list:List<LoseThing>){
        //item=list
    }

    private fun setUpImageView(){
        Glide.with(this)
            .load(R.drawable.add_lose_thing)
            .into(binding.addLoseThing)
    }

    private fun setOnImageViewListener(){
        binding.addLoseThing.setOnClickListener{
            Route.goPublishLoseThing(requireActivity())
        }
    }
    private fun refresh(){
        thread {
            Thread.sleep(2000)
            activity?.runOnUiThread{
                initListTest()
                loseAdapter.notifyDataSetChanged()
                //TODO
                binding.swipeRefresh.isRefreshing=false
            }
        }
    }
    companion object {
        @JvmStatic
        fun newInstance()=
            LoseThingFragment().apply {
            }
    }
}