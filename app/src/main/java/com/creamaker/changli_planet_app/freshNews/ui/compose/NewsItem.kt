package com.creamaker.changli_planet_app.freshNews.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.core.theme.AppTheme
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.FreshNewsItem

@Composable
fun NewsItem(
    item: FreshNewsItem,
    onImageClick: (List<String>, Int) -> Unit,
    onUserClick: (Int) -> Unit,
    onMenuClick: (FreshNewsItem) -> Unit,
    onLikeClick: (Int) -> Unit,
    onCommentClick: (FreshNewsItem) -> Unit,
    onShareClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.bgPrimaryColor)
            .clickable { }
            .padding(top = 15.dp, bottom = 0.dp) // Reset padding
            .padding(horizontal = 15.dp)
    ) {
        // Header: Avatar + Name + Time/Location + More
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.authorAvatar,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onUserClick(item.userId) },
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_fulilian),
                error = painterResource(id = R.drawable.ic_fulilian)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.authorName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.primaryTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.createTime,
                        fontSize = 12.sp,
                        color = AppTheme.colors.greyTextColor
                    )

                    if (item.location.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_one_point),
                            contentDescription = null,
                            modifier = Modifier.size(4.dp),
                            tint = AppTheme.colors.greyTextColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.location,
                            fontSize = 12.sp,
                            color = AppTheme.colors.greyTextColor
                        )
                    }
                }
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_more),
                contentDescription = "More",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onMenuClick(item) },
                tint = AppTheme.colors.iconSecondaryColor
            )
        }

        // Title
        if (item.title.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.primaryTextColor,
                lineHeight = 24.sp
            )
        }

        // Content
        if (item.content.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.content,
                fontSize = 15.sp,
                color = AppTheme.colors.primaryTextColor,
                lineHeight = 22.sp
            )
        }

        // Images Grid
        if (item.images.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            NewsImageGrid(
                images = item.images,
                onImageClick = { index -> onImageClick(item.images, index) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interaction Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Like
            InteractionButton(
                icon = if (item.isLiked) R.drawable.ic_news_liked else R.drawable.ic_like,
                count = item.liked,
                tint = if (item.isLiked) Color.Red else AppTheme.colors.iconSecondaryColor,
                onClick = { onLikeClick(item.freshNewsId) }
            )

            // Comment
            InteractionButton(
                icon = R.drawable.ic_comment,
                count = item.comments,
                tint = AppTheme.colors.iconSecondaryColor,
                onClick = { onCommentClick(item) }
            )

            // Collect/Share
            InteractionButton(
                icon = if (item.isFavorited) R.drawable.ic_collect else R.drawable.ic_un_collect,
                count = item.favoritesCount,
                tint = if (item.isFavorited) Color(0xFFFFD000) else AppTheme.colors.iconSecondaryColor,
                onClick = { onShareClick(item.freshNewsId) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = AppTheme.colors.bgSecondaryColor // Light gray separator
        )
    }
}

@Composable
fun NewsImageGrid(
    images: List<String>,
    onImageClick: (Int) -> Unit
) {
    val imageCount = images.size
    val rows = (imageCount + 2) / 3 // ceil(count / 3)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 0 until rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (j in 0 until 3) {
                    val index = i * 3 + j
                    if (index < imageCount) {
                        AsyncImage(
                            model = images[index],
                            contentDescription = "Image $index",
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onImageClick(index) },
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.under_construction), // Assuming there is a loading placeholder
                            error = painterResource(id = R.drawable.ic_error_vector) // Assuming error placeholder
                        )
                    } else {
                        // Empty spacer to transform weight correctly if less than 3 items in a row
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun InteractionButton(
    icon: Int,
    count: Int,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = tint
        )
        if (count > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = count.toString(),
                fontSize = 14.sp,
                color = AppTheme.colors.primaryTextColor
            )
        } else {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = " ", // Placeholder to keep height or just 0
                fontSize = 14.sp,
                color = AppTheme.colors.primaryTextColor
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun NewsItemPreview() {
    // 模拟数据 1：带图片的内容 (对应 JSON 第一条)
    val mockItemWithImages = FreshNewsItem(
        freshNewsId = 35,
        userId = 24,
        authorName = "长理吴彦祖", // JSON中没有，模拟一个名字
        authorAvatar = "", // 模拟头像URL，为空则显示占位图
        title = "你怎么知道我抽到一套安姐",
        content = "如图",
        images = listOf(
            "https://csustplant.obs.cn-south-1.myhuaweicloud.com/freshNewsImage/e23697db-7951-4208-a279-4ee04eaa390d.png",
            "https://csustplant.obs.cn-south-1.myhuaweicloud.com/freshNewsImage/c9122835-7712-4bec-b049-78160d961545.png",
            "https://csustplant.obs.cn-south-1.myhuaweicloud.com/freshNewsImage/fb46bbb0-1bf0-439f-b4d8-616c9b2e921f.png"
        ),
        tags = emptyList(),
        liked = 12, // 模拟点赞数
        comments = 5, // 模拟评论数
        createTime = "2025-12-10T13:37:30",
        allowComments = 1,
        favoritesCount = 0,
        location = "湖南", // 对应 JSON 的 address
        isLiked = true, // 模拟已点赞
        isFavorited = false
    )

    MaterialTheme {
        Column(
            modifier = Modifier
                .padding(vertical = 10.dp)
        ) {
            NewsItem(
                item = mockItemWithImages,
                onImageClick = { _, _ -> }, {}, {}, {}, {}, {}
            )
        }
    }
}

