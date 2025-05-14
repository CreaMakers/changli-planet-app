package com.example.changli_planet_app.Fragment

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
import com.example.changli_planet_app.Cache.UserInfoManager
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.Network.Resource
import com.example.changli_planet_app.Network.Response.FreshNewsItem
import com.example.changli_planet_app.Utils.GlideUtils
import com.example.changli_planet_app.Widget.Dialog.ShowImageDialog
import com.example.changli_planet_app.databinding.FragmentNewsBinding
import com.google.android.material.tabs.TabLayout
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
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
    private val pageSize: Int = 7
    private var isLoading = false
    private var hasMoreData = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewsBinding.inflate(layoutInflater)
        initObserve()
        initView()
        add.setOnClickListener {
            Route.goPublishFreshNews(requireActivity())
        }
        return binding.root
    }

    private fun initView() {
        adapter = FreshNewsAdapter(
            PlanetApplication.appContext,
            { imageUrl -> showImageDialog(imageUrl) },
            { Route.goUserHomeActivity(requireContext(), it) }
        )
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.apply {
            this.layoutManager = layoutManager
            adapter = this@NewsFragment.adapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (!isLoading && hasMoreData) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 4
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= pageSize
                        ) {
                            loadMoreData()
                        }
                    }
                }
            })
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshLayout.setOnRefreshListener { refreshLayout ->
            page = 1
            hasMoreData = true
            refreshNewsList(page, pageSize)
            if (refreshLayout.isRefreshing) refreshLayout.finishRefresh(5000)
        }

        refreshLayout.setOnLoadMoreListener { refreshLayout ->
            if (hasMoreData) {
                loadMoreData()
            } else {
                refreshLayout.finishLoadMoreWithNoMoreData()
            }
        }
    }

    private fun loadMoreData() {
        if (isLoading || !hasMoreData) return
        isLoading = true
        page++
        refreshNewsList(page, pageSize)
    }

    private fun initObserve() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        when (val freshNewsList = state.freshNewsList) {
                            is Resource.Success -> {
                                val newsList = freshNewsList.data
                                if (page == 1) {
                                    adapter.updateData(newsList)
                                    if (refreshLayout.isRefreshing) refreshLayout.finishRefresh()
                                    recyclerView.smoothScrollToPosition(0)
                                } else {
                                    if (newsList.isEmpty()) {
                                        hasMoreData = false
                                        refreshLayout.finishLoadMoreWithNoMoreData()
                                    } else {
                                        adapter.addData(newsList)
                                        refreshLayout.finishLoadMore()
                                    }
                                }
                                isLoading = false
                            }

                            is Resource.Error -> {
                                if (refreshLayout.isRefreshing) refreshLayout.finishRefresh(false)
                                refreshLayout.finishLoadMore(false)
                                isLoading = false
                            }

                            is Resource.Loading -> {
                                isLoading = true
                            }
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

    override fun onStart() {
        refreshLayout.autoRefresh()
        super.onStart()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            NewsFragment().apply {
            }
    }


    fun showImageDialog(imageUrl: String) {
        ShowImageDialog(requireContext(), imageUrl).show()
    }

    private fun refreshNewsList(page: Int, pageSize: Int) {
        viewModel.processIntent(FreshNewsContract.Intent.RefreshNewsByTime(page, pageSize))
    }
}