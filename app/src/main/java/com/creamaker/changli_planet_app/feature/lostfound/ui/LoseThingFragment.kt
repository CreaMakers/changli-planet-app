package com.creamaker.changli_planet_app.feature.lostfound.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.base.BaseFragment
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.databinding.FragmentLoseThingBinding
import com.creamaker.changli_planet_app.feature.lostfound.data.remote.dto.LoseThing
import com.creamaker.changli_planet_app.feature.lostfound.ui.adapter.LoseThingAdapter
import kotlin.concurrent.thread

class LoseThingFragment : BaseFragment<FragmentLoseThingBinding>() {
    private lateinit var loseAdapter: LoseThingAdapter
    private lateinit var item: List<LoseThing>

    override fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentLoseThingBinding {
        return FragmentLoseThingBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        initListTest()
        loseAdapter = LoseThingAdapter(item)
        binding.loseRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.loseRecyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )    //设置item之间的分割线
        binding.loseRecyclerView.adapter = loseAdapter

        setUpImageView()                                        //动态添加图片
        setOnImageViewListener()                                //添加addLoseThing的点击事件

        binding.swipeRefresh.setColorSchemeResources(R.color.black)
        binding.swipeRefresh.setOnRefreshListener {
            refresh()
        }
    }

    override fun initData() {
        // 数据初始化逻辑（如果有的话）
    }

    override fun initObserve() {
        // 观察者初始化逻辑（如果有的话）
    }

    private fun initListTest() {
        val longText = "nya~,一个甘城猫，在12教210教室不见了www一个甘城猫，在12教210教室不见了www一个甘城猫，在12教210教室不见了www一个甘城猫，在12教210教室不见了www一个甘城猫，在12教210教室不见了www一个甘城猫，在12教210教室不见了www一个甘城猫，在12教210教室不见了www一个甘城猫，在12教210教室不见了www"
        // TODO: 初始化测试数据
        item = emptyList() // 这里需要根据实际情况初始化数据
    }

    fun setItem(list: List<LoseThing>) {
        // item = list
        // TODO: 如果需要更新数据，可以在这里实现
    }

    private fun setUpImageView() {
        Glide.with(this)
            .load(R.drawable.add_lose_thing)
            .into(binding.addLoseThing)
    }

    private fun setOnImageViewListener() {
        binding.addLoseThing.setOnClickListener {
            Route.goPublishLoseThing(requireActivity())
        }
    }

    private fun refresh() {
        thread {
            Thread.sleep(2000)
            activity?.runOnUiThread {
                initListTest()
                loseAdapter.notifyDataSetChanged()
                // TODO
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = LoseThingFragment()
    }
}