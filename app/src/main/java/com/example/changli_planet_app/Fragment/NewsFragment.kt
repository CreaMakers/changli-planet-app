package com.example.changli_planet_app.Fragment

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Activity.Contract.FreshNewsContract
import com.example.changli_planet_app.Activity.ViewModel.FreshNewsViewModel
import com.example.changli_planet_app.Adapter.FreshNewsAdapter
import com.example.changli_planet_app.Adapter.ViewHolder.FreshNewsItemViewModel
import com.example.changli_planet_app.Cache.UserInfoManager
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.Network.Resource
import com.example.changli_planet_app.Network.Response.FreshNewsItem
import com.example.changli_planet_app.Network.repository.FreshNewsRepository
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Util.GlideUtils
import com.example.changli_planet_app.Widget.Dialog.ShowImageDialog
import com.example.changli_planet_app.databinding.FragmentNewsBinding
import com.google.android.material.tabs.TabLayout
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class NewsFragment : Fragment() {
    private lateinit var binding: FragmentNewsBinding
    private val refreshLayout: SmartRefreshLayout by lazy { binding.refreshLayout }
    private val add: ImageView by lazy { binding.add }
    private val recyclerView: RecyclerView by lazy { binding.newsRecyclerView }
    private val avatar by lazy { binding.newsAvatar }
    private val to: TabLayout by lazy { binding.to }

    private val viewModel: FreshNewsViewModel by viewModels()
    private lateinit var adapter: FreshNewsAdapter
    private var page: Int = 1
    private val pageSize: Int = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewsBinding.inflate(layoutInflater)
        initObserve()
        adapter = FreshNewsAdapter(
            PlanetApplication.appContext,
            { imageUrl -> showImageDialog(imageUrl) },
            {}
        )
        recyclerView.layoutManager = LinearLayoutManager(PlanetApplication.appContext)
        recyclerView.adapter = adapter

        add.setOnClickListener {
            Route.goPublishFreshNews(requireActivity())
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refreshLayout.autoRefresh()      //自动进行第一次刷新

        refreshLayout.setOnRefreshListener(object : OnRefreshListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                refreshNewsList(page, pageSize)
                if (refreshLayout.isRefreshing) refreshLayout.finishRefresh(10000)
                //设置最长刷新时间为10s
            }
        })
        refreshLayout.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                refreshLayout.finishLoadMore(1000)
            }
        })
    }

    private fun initObserve() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        if (state.freshNewsList is Resource.Success<List<FreshNewsItem>>) {
                            adapter.updateData((state.freshNewsList as Resource.Success<List<FreshNewsItem>>).data)
                            if (refreshLayout.isRefreshing) refreshLayout.finishRefresh()
                            recyclerView.smoothScrollToPosition(0) // 滚动到顶部
                            //更新完数据后结束刷新动画
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        GlideUtils.load(this, avatar, UserInfoManager.userAvatar)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            NewsFragment().apply {
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onRefreshEvent(event: FreshNewsContract.Event) {
        when (event) {
            is FreshNewsContract.Event.RefreshNewsList -> {
                page = 1 // 重置页码
                refreshLayout.autoRefresh() // 自动刷新
            }

            else -> {}
        }
    }

    fun showImageDialog(imageUrl: String) {
        ShowImageDialog(requireContext(), imageUrl).show()
    }

    private fun refreshNewsList(page: Int, pageSize: Int) {
        viewModel.processIntent(FreshNewsContract.Intent.RefreshNewsByTime(page, pageSize))
    }
}