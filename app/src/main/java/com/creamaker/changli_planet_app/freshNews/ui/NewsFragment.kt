package com.creamaker.changli_planet_app.freshNews.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.freshNews.contract.FreshNewsContract
import com.creamaker.changli_planet_app.freshNews.ui.compose.FreshNewsScreen
import com.creamaker.changli_planet_app.freshNews.viewModel.FreshNewsViewModel
import com.creamaker.changli_planet_app.utils.PlanetConst
import com.creamaker.changli_planet_app.widget.dialog.ImageSliderDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class NewsFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = NewsFragment()
        private const val TAG = "NewsFragment"
    }

    private val viewModel: FreshNewsViewModel by viewModels()

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, " 新鲜事收到返回结果:${result.resultCode}")
            when (result.resultCode) {
                PlanetConst.RESULT_OK -> {
                    Log.d(TAG, "评论发布成功，刷新列表")
                    val data: Intent? = result.data
                    data?.let {
                        val newsId = it.getIntExtra("freshNewsId", -1)
                        val newCommentCount = it.getIntExtra("newLevel1CommentsCount", -1)
                        if (newsId != -1 && newCommentCount != -1) {
                            viewModel.processIntent(
                                FreshNewsContract.Intent.UpdateLocalCommentCount(
                                    newsId,
                                    newCommentCount
                                )
                            )
                        }
                    }
                }
                PlanetConst.RESULT_OK_NEWS_REFRESH -> {
                    val data: Intent? = result.data
                    data?.let {
                        val newAccount = it.getStringExtra("account")
                        val newAvatarUrl = it.getStringExtra("avatarUrl")
                        val userId = it.getIntExtra("userId", -1)
                        if (userId != -1 && newAccount != null && newAvatarUrl != null) {
                            viewModel.processIntent(
                                FreshNewsContract.Intent.UpdateLocalUserInfo(
                                    userId,
                                    newAccount,
                                    newAvatarUrl
                                )
                            )
                        }
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        EventBus.getDefault().register(this)
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppSkinTheme {
                    FreshNewsScreen(
                        viewModel = viewModel,
                        onPublishClick = {
                            val intent =
                                Intent(requireContext(), PublishFreshNewsActivity::class.java)
                            startActivity(intent)
                        },
                        onImageClick = { imageList, position ->
                            ImageSliderDialog(requireContext(), imageList, position).show()
                        },
                        onUserClick = { userId ->
                            startForResult.launch(
                                Intent(
                                    requireContext(),
                                    UserHomeActivity::class.java
                                ).apply {
                                    putExtra("userId", userId)
                                })
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
    }

    private fun initData() {
        // Initial load
        viewModel.processIntent(FreshNewsContract.Intent.RefreshNewsByTime(1, 10))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun openComments(event: FreshNewsContract.Event) {
        when(event){
            is FreshNewsContract.Event.openComments ->
                startForResult.launch(Intent(requireContext(), CommentsActivity::class.java))
            else -> {}
        }
    }

    @Subscribe
    fun reFreshNews(event: FreshNewsContract.Event) {
        when(event){
            is FreshNewsContract.Event.RefreshNewsList ->
                viewModel.processIntent(FreshNewsContract.Intent.RefreshNewsByTime(1, 10))
            else -> {}
        }
    }
}