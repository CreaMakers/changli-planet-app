package com.example.changli_planet_app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.changli_planet_app.Adapter.FoundThingAdapter
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.Data.jsonbean.FoundThing
import com.example.changli_planet_app.Data.jsonbean.LoseThing
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.FragmentFoundThingBinding
import kotlin.concurrent.thread

class FoundThingFragment:Fragment() {
    private lateinit var binding:FragmentFoundThingBinding
    private lateinit var foundAdapter: FoundThingAdapter
    private lateinit var item:List<FoundThing>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initListTest()
        binding=FragmentFoundThingBinding.inflate(layoutInflater)
        foundAdapter= FoundThingAdapter(item)
        binding.foundRecyclerView.layoutManager=LinearLayoutManager(context)
        binding.foundRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))    //设置item之间的分割线
        binding.foundRecyclerView.adapter=foundAdapter
        setUpImageView()
        setOnImageViewListener()

        binding.swipeRefresh.setColorSchemeResources(R.color.black)
        binding.swipeRefresh.setOnRefreshListener {
            refresh()
        }
        return binding.root
    }

    private fun initListTest(){
        item=listOf(FoundThing(R.drawable.nmap,"何同学","计算机与通行工程学院","一只猫","一个甘城猫，在12教210教室不见了www",R.drawable.nmap),
            FoundThing(R.drawable.nmap,"何同学","计算机与通行工程学院","一只猫","一个甘城猫，在12教210教室不见了www",R.drawable.nmap),
            FoundThing(R.drawable.nmap,"何同学","计算机与通行工程学院","一只猫","一个甘城猫，在12教210教室不见了www",R.drawable.nmap),
            FoundThing(R.drawable.nmap,"何同学","计算机与通行工程学院","一只猫","一个甘城猫，在12教210教室不见了www2024 10 12 10：12：10\",\"一只猫\",\"一个甘城猫，在12教210教室不见了www2024 10 12 10：12：10\",\"一只猫\",\"一个甘城猫，在12教210教室不见了www2024 10 12 10：12：10\",\"一只猫\",\"一个甘城猫，在12教210教室不见了www"),
            FoundThing(R.drawable.nmap,"何同学","计算机与通行工程学院","一只猫","一个甘城猫，在12教210教室不见了www",R.drawable.nmap),
            FoundThing(R.drawable.nmap,"何同学","计算机与通行工程学院","一只猫","一个甘城猫，在12教210教室不见了www",R.drawable.nmap),
            FoundThing(R.drawable.nmap,"何同学","计算机与通行工程学院","一只猫","一个甘城猫，在12教210教室不见了www",R.drawable.nmap),
            FoundThing(R.drawable.nmap,"何同学","计算机与通行工程学院","一只猫","一个甘城猫，在12教210教室不见了www"),
            FoundThing(R.drawable.nmap,"何同学","计算机与通行工程学院","一只猫","一个甘城猫，在12教210教室不见了www",R.drawable.nmap),
            FoundThing(R.drawable.nmap,"何同学","计算机与通行工程学院","一只猫","一个甘城猫，在12教210教室不见了www"),
            FoundThing(R.drawable.nmap,"何同学","计算机与通行工程学院","一只猫","一个甘城猫，在12教210教室不见了www"),
            FoundThing(R.drawable.nmap,"何同学","计算机与通行工程学院","一只猫","一个甘城猫，在12教210教室不见了www"),)
    }
    private fun refresh() {
        thread {
            Thread.sleep(2000)
            activity?.runOnUiThread{
                initListTest()
                foundAdapter.notifyDataSetChanged()
                //TODO
                binding.swipeRefresh.isRefreshing=false
            }
        }
    }

    fun setItem(list:List<LoseThing>){
        //item=list
    }

    fun setUpImageView(){                           //动态添加图片
        Glide.with(this)
            .load(R.drawable.add_found_thing)
            .into(binding.addFoundThing)
    }

    fun setOnImageViewListener(){                   //添加addFoundThing的点击事件
        binding.addFoundThing.setOnClickListener{
            Route.goPublishFoundThing(requireActivity())
        }
    }

    companion object {
        @JvmStatic
        fun newInstance()=
            FoundThingFragment().apply {
            }
    }
}