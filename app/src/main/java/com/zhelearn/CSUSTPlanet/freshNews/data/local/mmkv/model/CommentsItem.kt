package com.zhelearn.CSUSTPlanet.freshNews.data.local.mmkv.model

data class CommentsItem(
    var freshNewsItem: FreshNewsItem?,
    var level1CommentsResults: List<CommentsResult>
)