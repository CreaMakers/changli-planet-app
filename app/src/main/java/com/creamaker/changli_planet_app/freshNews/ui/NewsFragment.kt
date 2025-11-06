package com.creamaker.changli_planet_app.freshNews.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.creamaker.changli_planet_app.base.BaseFragment
import com.creamaker.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.core.mvi.observeState
import com.creamaker.changli_planet_app.core.network.ApiResponse
import com.creamaker.changli_planet_app.databinding.FragmentNewsBinding
import com.creamaker.changli_planet_app.freshNews.contract.FreshNewsContract
import com.creamaker.changli_planet_app.freshNews.ui.adapter.FreshNewsAdapter
import com.creamaker.changli_planet_app.freshNews.viewModel.FreshNewsViewModel
import com.creamaker.changli_planet_app.utils.GlideUtils
import com.creamaker.changli_planet_app.utils.ItemDecorationWrapper
import com.creamaker.changli_planet_app.utils.PlanetConst
import com.creamaker.changli_planet_app.widget.dialog.ImageSliderDialog
import com.creamaker.changli_planet_app.widget.dialog.ShowImageDialog
import com.creamaker.changli_planet_app.widget.view.AddNewsFloats
import com.creamaker.changli_planet_app.widget.view.CustomToast
import com.google.android.material.tabs.TabLayout
import com.gradle.scan.agent.serialization.scan.serializer.kryo.by
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import kotlin.jvm.java

class NewsFragment : BaseFragment<FragmentNewsBinding>() {

    companion object {
        @JvmStatic
        fun newInstance() = NewsFragment()
        // 新鲜事每个item底部margin dp
        private const val NEWS_ITEM_BOTTOM_MARGIN = 8f
    }

    private val refreshLayout: SmartRefreshLayout by lazy { binding.refreshLayout }
    private val recyclerView: RecyclerView by lazy { binding.newsRecyclerView }
    private val avatar by lazy { binding.newsAvatar }
    private val to: TabLayout by lazy { binding.to }
    private var mFloatView: AddNewsFloats? = null
    private val newsSearch: LinearLayout by lazy { binding.newsSearch }
    private val viewModel: FreshNewsViewModel by viewModels()
    private lateinit var adapter: FreshNewsAdapter
    private var page: Int = 1
    private val pageSize: Int = 10
    private var isLoading = false
    private var hasMoreData = true

    // 前往addActivity后的返回调用
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, " 新鲜事收到返回结果:${result.resultCode}")
            when (result.resultCode) {
                PlanetConst.RESULT_OK -> {
                    Log.d(TAG, "评论发布成功，刷新列表")
                    refreshLayout.autoRefresh()
                }
                PlanetConst.RESULT_OK_NEWS_REFRESH -> {
                    val data: Intent? = result.data
                    data?.let {
                        val newAccount = it.getStringExtra("account")
                        val newAvatarUrl = it.getStringExtra("avatarUrl")
                        val userId = it.getIntExtra("userId", -1)
                        if (userId != -1 && newAccount != null && newAvatarUrl != null
                            && !TextUtils.isEmpty(newAccount) && !TextUtils.isEmpty(newAvatarUrl)
                        ) {
                            adapter.updateDataByUserId(userId, newAccount, newAvatarUrl)
                        }
                    }
                }
            }
        }

    override fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentNewsBinding {
        return FragmentNewsBinding.inflate(inflater, container, false)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        EventBus.getDefault().register(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    override fun initView() {
//        binding.ivUnderConstruction.load(R.drawable.under_construction)
        addFloatView()
        mFloatView?.setOnClickListener {
            Log.d(TAG, " 点击新鲜事悬浮窗")
            val intent = Intent(requireContext(), PublishFreshNewsActivity::class.java)
//            startForResult.launch(intent)
            startActivity(intent)
        }
        newsSearch.setOnClickListener {
            CustomToast.showMessage(requireContext(), "搜索功能正在开发中，敬请期待！")
        }

        adapter = FreshNewsAdapter(
            PlanetApplication.Companion.appContext,
            onImageClick = { imageList, position ->
                Log.d(TAG, "点击图片列表：$imageList ,位置：$position")
                ImageSliderDialog(requireContext(), imageList, position).show()
            },
            onUserClick = { userId ->
                startForResult.launch(Intent(requireContext(), UserHomeActivity::class.java).apply {
                    putExtra("userId", userId)
                })
            },
            onMenuClick = { newsItem ->
                // 菜单点击处理
            },
            onLikeClick = { newsItem ->
                viewModel.processIntent(FreshNewsContract.Intent.LikeNews(newsItem))
            },
            onCommentClick = { newsItem ->
                // 评论点击处理
                viewModel.processIntent(FreshNewsContract.Intent.OpenComments(newsItem) )
            },
            onCollectClick = { newsItem ->
                viewModel.processIntent(FreshNewsContract.Intent.FavoriteNews(newsItem))
            }
        )

        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.apply {
            this.layoutManager = layoutManager
            this.adapter = this@NewsFragment.adapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (!isLoading && hasMoreData) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= pageSize
                        ) {
                            loadMoreData()
                        }
                    }
                }
            })
            // 设置item的底部margin
            addItemDecoration(
                ItemDecorationWrapper.Builder(requireContext())
                    .setVerticalMarginDp(0f, NEWS_ITEM_BOTTOM_MARGIN)
                    .setMarginCondition { position, parent ->
                        // 最后一个也不需要
                        position != parent.adapter?.itemCount?.minus(1)
                    }
                    .build()
            )
        }

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

        refreshLayout.autoRefresh()
    }

    override fun initData() {
        // 数据初始化逻辑（如果有的话）
    }

    override fun initObserve() {
        viewLifecycleOwner.lifecycleScope.launch {.3
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.run {
                    observeState({ value.freshNewsList }) {
                        when (val freshNewsList = it) {

                            is ApiResponse.Success -> {
                                Log.d(TAG, "新鲜事刷新成功")
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

                            is ApiResponse.Error -> {
                                if (refreshLayout.isRefreshing) refreshLayout.finishRefresh(false)
                                refreshLayout.finishLoadMore(false)
                                isLoading = false
                            }

                            is ApiResponse.Loading -> {
                                isLoading = true
                            }
                        }
                        addFloatView()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        GlideUtils.load(this, avatar, UserInfoManager.userAvatar)
    }

    fun showImageDialog(imageUrl: String) {
        ShowImageDialog(requireContext(), imageUrl).show()
    }

    private fun refreshNewsList(page: Int, pageSize: Int) {
        viewModel.processIntent(FreshNewsContract.Intent.RefreshNewsByTime(page, pageSize))
    }

    private fun loadMoreData() {
        if (isLoading || !hasMoreData) return
        isLoading = true
        page++
        refreshNewsList(page, pageSize)
    }

    private fun addFloatView() {
        if (mFloatView != null) {
            (mFloatView?.parent as? ViewGroup)?.removeView(mFloatView)
        }
        // 创建新的悬浮窗
        mFloatView = AddNewsFloats(requireContext())

        mFloatView?.setOnFloatClickListener { view ->
            Route.goPublishFreshNews(requireContext())
        }

        // 设置初始位置 (右下角)
        mFloatView?.x = resources.displayMetrics.widthPixels - 200f
        mFloatView?.y = resources.displayMetrics.heightPixels - 500f
        mFloatView?.elevation = 100f
        binding.root.addView(mFloatView)
    }
    @Subscribe
    fun openComments(event: FreshNewsContract.Event) {
        when(event){
            is FreshNewsContract.Event.openComments ->
                startForResult.launch(Intent(requireContext(),CommentsActivity::class.java))
            else -> {}
        }

    }
    @Subscribe
    fun reFreshNews(event: FreshNewsContract.Event) {
        when(event){
            is FreshNewsContract.Event.RefreshNewsList ->
                refreshLayout.autoRefresh()
            else -> {}
        }

    }
}