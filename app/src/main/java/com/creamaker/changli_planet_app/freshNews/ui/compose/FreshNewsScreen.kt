package com.creamaker.changli_planet_app.freshNews.ui.compose

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.core.network.ApiResponse
import com.creamaker.changli_planet_app.core.theme.AppTheme
import com.creamaker.changli_planet_app.freshNews.contract.FreshNewsContract.Intent.FavoriteNews
import com.creamaker.changli_planet_app.freshNews.contract.FreshNewsContract.Intent.LikeNews
import com.creamaker.changli_planet_app.freshNews.contract.FreshNewsContract.Intent.OpenComments
import com.creamaker.changli_planet_app.freshNews.contract.FreshNewsContract.Intent.RefreshNewsByTime
import com.creamaker.changli_planet_app.freshNews.contract.FreshNewsContract.Intent.UpdateTabIndex
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.FreshNewsItem
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.FreshNewsItemResult
import com.creamaker.changli_planet_app.freshNews.viewModel.FreshNewsViewModel
import kotlinx.coroutines.launch

@Composable
fun FreshNewsScreen(
    viewModel: FreshNewsViewModel,
    onPublishClick: () -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    onUserClick: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val isLoading = state.freshNewsListResults is ApiResponse.Loading
    val isRefreshing = isLoading
    val isLoadingMore = isLoading && state.page > 1

    // Show Back to Top
    val showBackToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }

    // Load More Logic
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex =
                listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItemsCount > 5 && lastVisibleItemIndex >= totalItemsCount - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !isLoading) {
            viewModel.processIntent(RefreshNewsByTime(state.page + 1, 10))
        }
    }

    FreshNewsContent(
        modifier = Modifier,
        currentTab = state.currentTab,
        freshNewsListResult = state.freshNewsListResults,
        isLoadingMore = isLoadingMore,
        listState = listState,
        showBackToTop = showBackToTop,
        onTabSelected = { index ->
            viewModel.processIntent(UpdateTabIndex(index))
        },
        onSearchClick = {
            Toast.makeText(context, "搜索功能正在开发中，敬请期待！", Toast.LENGTH_SHORT).show()
        },
        onRefresh = {
            viewModel.processIntent(RefreshNewsByTime(1, 10))
        },
        onScrollToTop = {
            scope.launch {
                listState.animateScrollToItem(0)
            }
        },
        onPublishClick = onPublishClick,
        onImageClick = onImageClick,
        onUserClick = onUserClick,
        onLikeClick = { id -> viewModel.processIntent(LikeNews(id)) },
        onCommentClick = { item -> viewModel.processIntent(OpenComments(item)) },
        onShareClick = { id -> viewModel.processIntent(FavoriteNews(id)) }
    )
}

@Composable
fun FreshNewsContent(
    modifier: Modifier = Modifier,
    currentTab: Int,
    freshNewsListResult: ApiResponse<List<FreshNewsItemResult>>,
    isLoadingMore: Boolean,
    listState: LazyListState,
    showBackToTop: Boolean,
    onTabSelected: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onRefresh: () -> Unit,
    onScrollToTop: () -> Unit,
    onPublishClick: () -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    onUserClick: (Int) -> Unit,
    onLikeClick: (Int) -> Unit,
    onCommentClick: (FreshNewsItem) -> Unit,
    onShareClick: (Int) -> Unit
) {
    val isRefreshing = freshNewsListResult is ApiResponse.Loading
    val state = rememberPullToRefreshState()
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        containerColor = AppTheme.colors.bgSecondaryColor,
        topBar = {
            NewsTopBar(
                currentTab = currentTab,
                onTabSelected = onTabSelected,
                onSearchClick = onSearchClick
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(
                    onClick = onPublishClick,
                    containerColor = AppTheme.colors.bgPrimaryColor,
                    modifier = Modifier.padding(bottom = 80.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_blue_add),
                        contentDescription = "Publish",
                        tint = AppTheme.colors.iconSecondaryColor,
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            PullToRefreshBox(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.bgPrimaryColor), // White background for flat look
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = state,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = state,
                        isRefreshing = isRefreshing,
                        containerColor = AppTheme.colors.bgPrimaryColor,
                        color = AppTheme.colors.commonColor,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = paddingValues.calculateTopPadding())
                    )
                }
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding(),
                        bottom = 100.dp + paddingValues.calculateBottomPadding(),
                        start = 0.dp,
                        end = 0.dp
                    ),
                    verticalArrangement = Arrangement.Top
                ) {
                    when (freshNewsListResult) {
                        is ApiResponse.Loading -> {
                            items(2) {
                                NewsSkeletonItem()
                            }
                        }

                        is ApiResponse.Success -> {
                            val listData = freshNewsListResult.data
                            items(listData, key = { item ->
                                when (item) {
                                    is FreshNewsItemResult.Success -> item.freshNewsItem.freshNewsId
                                    is FreshNewsItemResult.NoMore -> "no_more_footer_key"
                                    else -> item.hashCode()
                                }
                            }) { itemResult ->
                                when (itemResult) {
                                    is FreshNewsItemResult.Success -> {
                                        NewsItem(
                                            item = itemResult.freshNewsItem,
                                            onImageClick = onImageClick,
                                            onUserClick = onUserClick,
                                            onMenuClick = { /* Menu */ },
                                            onLikeClick = onLikeClick,
                                            onCommentClick = onCommentClick,
                                            onShareClick = onShareClick
                                        )
                                    }

                                    is FreshNewsItemResult.NoMore -> {
                                        Text(
                                            text = "没有更多了",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            color = AppTheme.colors.greyTextColor,
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    else -> {}
                                }
                            }

                            if (isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = AppTheme.colors.commonColor,
                                            trackColor = AppTheme.colors.bgPrimaryColor
                                        )
                                    }
                                }
                            }
                        }

                        is ApiResponse.Error -> {
                            items(2) {
                                NewsSkeletonItem()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Fresh News Screen Preview")
@Composable
fun PreviewFreshNewsContent() {
    MaterialTheme {
        FreshNewsContent(
            currentTab = 0,
            freshNewsListResult = ApiResponse.Success(emptyList()),
            isLoadingMore = true,
            listState = rememberLazyListState(),
            showBackToTop = true,
            onTabSelected = {},
            onSearchClick = {},
            onRefresh = {},
            onScrollToTop = {},
            onPublishClick = {},
            onImageClick = { _, _ -> },
            onUserClick = {},
            onLikeClick = {},
            onCommentClick = {},
            onShareClick = {}
        )
    }
}
