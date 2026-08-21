package com.zhelearn.CSUSTPlanet.freshNews.data.local.mmkv.model

sealed class FreshNewsItemResult {
    data object Loading : FreshNewsItemResult()
    data class Success(val freshNewsItem: FreshNewsItem) : FreshNewsItemResult()
    data class Error(val errorMessage: String) : FreshNewsItemResult()
    data object NoMore : FreshNewsItemResult()
}