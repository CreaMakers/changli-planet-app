package com.creamaker.changli_planet_app.skin.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.core.theme.AppTheme
import com.creamaker.changli_planet_app.skin.data.model.Skin
import com.creamaker.changli_planet_app.skin.viewmodel.SkinSelectionViewModel

class SkinSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppSkinTheme { SkinSelectionScreen(onBackClick = { finish() }) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinSelectionScreen(viewModel: SkinSelectionViewModel = viewModel(), onBackClick: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyGridState()

    // 分页检测：滑动到底部加载更多
    val layoutInfo by remember { derivedStateOf { scrollState.layoutInfo } }
    LaunchedEffect(layoutInfo) {
        // 只有当还有更多数据时才触发加载
        if (!uiState.hasMore) return@LaunchedEffect

        val totalItems = layoutInfo.totalItemsCount
        val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

        // 预加载阈值：倒数第4个item可见时加载
        if (totalItems > 0 && lastVisibleItemIndex >= totalItems - 4) {
            viewModel.loadNextPage()
        }
    }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = {
                            Text(PlanetApplication.appContext.getString(R.string.skin_center))
                        },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(painterResource(id = R.drawable.back), contentDescription = "Back")
                            }
                        },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = AppTheme.colors.bgTopBarColor,
                                        titleContentColor = AppTheme.colors.titleTopColor,
                                        navigationIconContentColor = AppTheme.colors.titleTopColor
                                )
                )
            }
    ) { paddingValues ->
        Box(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(paddingValues)
                                .background(AppTheme.colors.bgPrimaryColor)
        ) {
            if (uiState.error != null) {
                // 简单的错误提示，点击重试逻辑可根据需要添加
                Text(
                        text = uiState.error!!,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                )
            }

            LazyVerticalGrid(
                    columns = GridCells.Fixed(2), // 每一行2个
                    state = scrollState,
                    contentPadding = PaddingValues(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
            ) {
                // 1. 正常的皮肤列表 Item
                itemsIndexed(uiState.skinList) { _, skin ->
                    SkinItemView(
                            skin = skin,
                            currentUsingSkinName = uiState.currentUsingSkin,
                            onApplyClick = { viewModel.applySkin(it) }
                    )
                }

                // 2. 底部状态栏 Item (加载中 或 没有更多)
                // span = { GridItemSpan(2) } 让它占满一行
                item(span = { GridItemSpan(2) }) {
                    Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoading) {
                            // 正在加载时显示转圈
                            CircularProgressIndicator(modifier = Modifier.size(30.dp))
                        } else if (!uiState.hasMore) {
                            // 没有更多数据时显示提示文本
                            Text(
                                    text =
                                            PlanetApplication.appContext.getString(
                                                    R.string.skin_no_more
                                            ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                            )
                        } else {
                            // 还有更多数据但没在加载（通常不会长时间处于此状态，除非加载失败）
                            // 可以留空或者放一个 Spacer
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SkinItemView(skin: Skin, currentUsingSkinName: String, onApplyClick: (Skin) -> Unit) {
    val isUsing = skin.name == currentUsingSkinName

    Card(
            modifier = Modifier.fillMaxWidth().height(220.dp), // 固定高度以保证对齐
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.bgSecondaryColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 图片区域
                AsyncImage(
                        model =
                                ImageRequest.Builder(LocalContext.current)
                                        .data(skin.imageUrl)
                                        .crossfade(true)
                                        .build(),
                        contentDescription = skin.name,
                        contentScale = ContentScale.Fit,
                        modifier =
                                Modifier.fillMaxWidth()
                                        .weight(1f) // 占据大部分空间
                                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                        .background(Color.LightGray) // 占位背景
                )

                // 底部信息区域
                Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Text(
                            text = skin.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                            text = skin.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                    )
                    // 预留按钮空间
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }

            // 右下角按钮
            Button(
                    onClick = { onApplyClick(skin) },
                    enabled = !isUsing, // 如果正在使用则不可点击
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).height(36.dp),
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor = AppTheme.colors.bgButtonColor
                            )
            ) {
                Text(
                        text =
                                with(PlanetApplication.appContext) {
                                    if (isUsing) getString(R.string.skin_using)
                                    else getString(R.string.skin_use)
                                },
                        fontSize = 12.sp,
                        color =
                                if (!isUsing) AppTheme.colors.textButtonColor
                                else AppTheme.colors.textHeighLightColor
                )
            }
        }
    }
}
