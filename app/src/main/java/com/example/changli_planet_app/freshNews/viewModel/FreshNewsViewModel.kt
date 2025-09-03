package com.example.changli_planet_app.freshNews.viewModel

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.changli_planet_app.freshNews.data.local.mmkv.RefreshNewsCache
import com.example.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.example.changli_planet_app.core.coroutineContext.ErrorCoroutineContext
import com.example.changli_planet_app.core.mvi.MviViewModel
import com.example.changli_planet_app.core.PlanetApplication
import com.example.changli_planet_app.freshNews.contract.FreshNewsContract
import com.example.changli_planet_app.core.network.Resource
import com.example.changli_planet_app.freshNews.data.local.mmkv.model.FreshNewsItem
import com.example.changli_planet_app.freshNews.data.local.mmkv.model.FreshNewsPublish
import com.example.changli_planet_app.freshNews.data.remote.repository.FreshNewsRepository
import com.example.changli_planet_app.freshNews.data.remote.repository.UserProfileRepository
import com.example.changli_planet_app.widget.View.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.io.File

class FreshNewsViewModel : MviViewModel<FreshNewsContract.Intent, FreshNewsContract.State>() {
    private val handler = Handler(Looper.getMainLooper())

    override fun processIntent(intent: FreshNewsContract.Intent) {
        when (intent) {
            is FreshNewsContract.Intent.InputMessage -> inputMessage(intent.value, intent.type)
            is FreshNewsContract.Intent.AddImage -> addImage(intent.file)
            is FreshNewsContract.Intent.RemoveImage -> removeImage(intent.index)
            is FreshNewsContract.Intent.Publish -> publish()
            is FreshNewsContract.Intent.ClearAll -> clearAll()
            is FreshNewsContract.Intent.RefreshNewsByTime -> refreshNewsByTime(
                intent.page,
                intent.pageSize
            )

            is FreshNewsContract.Intent.UpdateUserProfile -> refreshUserProfileByNetwork(intent.userId)
            is FreshNewsContract.Intent.UpdateTabIndex -> changeCurrentTab(intent.currentIndex)
            is FreshNewsContract.Intent.Initialization -> {}

            is FreshNewsContract.Intent.LikeNews -> likeNewsItem(intent.freshNewsItem)
            is FreshNewsContract.Intent.FavoriteNews -> favoriteNewsItem(intent.freshNewsItem)
        }

    }

    // 给State初始值
    override fun initialState() = FreshNewsContract.State(
        0,
        Resource.Loading(),
        FreshNewsPublish(),
        mutableListOf(),
        false,
        0,
    )

    private fun changeCurrentTab(currentTab: Int) {
        viewModelScope.launch(ErrorCoroutineContext()) {

        }
        updateState {
            copy(currentTab = currentTab)
        }
    }

    private fun inputMessage(value: Any, type: String) {
        updateState {
            when (type) {
                "title" -> state.value.publishNews.title = value as String
                "content" -> state.value.publishNews.content = value as String
            }
            copy(
                isEnable = checkEnable()
            )
        }
    }

    private fun addImage(file: File) {
        updateState {
            state.value.images.add(file)
            copy()
        }
    }

    private fun removeImage(index: Int) {
        updateState {
            state.value.images.removeAt(index)
            copy()
        }
    }

    private fun checkEnable(): Boolean = with(state.value.publishNews) {
        title.isNotEmpty() && content.isNotEmpty()
    }

    private fun publish() {
        viewModelScope.launch {
            state.value.publishNews.user_id = UserInfoManager.userId
            /*val client= OkHttpClient()

            val requestBody= MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .apply {
                    //添加文件数组
                    if(state.value.images.size>0){
                        state.value.images.forEach { file->
                            addFormDataPart(
                                "images",
                                file.name,
                                file.asRequestBody("image/*".toMediaType())
                            )
                        }
                    }else{
                        // 添加空字段
                        addFormDataPart("images", "",ByteArray(0).toRequestBody("application/octet-stream".toMediaType()) )
                    }

                    //添加新鲜事对象
                    addFormDataPart(
                        "fresh_news",
                        null,
                        Gson().toJson(state.value.publishNews).toRequestBody("application/json".toMediaType())
                    )
                }
                .build()

            val request= Request.Builder()
                .url(PlanetApplication.FreshNewsIp)
                .post(requestBody)
                .header("deviceId",PlanetApplication.deviceId)
                .header("Authorization",PlanetApplication.accessToken?:"")
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                override fun onResponse(call: Call, response: Response) {
                    val type=object : TypeToken<MyResponse<FreshNewsItem>>(){}.type
                    val formJson=Gson().fromJson<MyResponse<FreshNewsItem>>(response.body?.string(),type)
                    when(formJson.code){
                        "200"->{
                            clearAll()
                            EventBus.getDefault().post(FreshNewsContract.Event.closePublish)
                            handler.post{
                                CustomToast.showMessage(
                                    PlanetApplication.appContext,
                                    "发布成功"
                                )
                            }
                        }
                        else->{
                            handler.post{
                                CustomToast.showMessage(
                                    PlanetApplication.appContext,
                                    "发布失败"
                                )
                            }

                        }
                    }
                }
            })
            */*/
            val result = FreshNewsRepository.Companion.instance.postFreshNews(
                state.value.images,
                state.value.publishNews
            )
            result.onEach { response ->
                when (response.code) {
                    "200" -> {
                        clearAll()
                        cleanupTempFiles(state.value.images) //清理临时压缩文件
                        EventBus.getDefault().post(FreshNewsContract.Event.closePublish)    //关闭发布界面
                        handler.post {
                            CustomToast.Companion.showMessage(
                                PlanetApplication.Companion.appContext,
                                "发布成功"
                            )
                        }
                    }

                    else -> {
                        handler.post {
                            CustomToast.Companion.showMessage(
                                PlanetApplication.Companion.appContext,
                                "发布失败"
                            )
                        }
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun clearAll() {
        updateState {
            copy(
                images = mutableListOf(),
                isEnable = false,
                publishNews = FreshNewsPublish()
            )
        }
    }

    private fun refreshNewsByTime(page: Int, pageSize: Int) {
        viewModelScope.launch {
            updateState { copy(freshNewsList = Resource.Loading()) }

            val newsResult = FreshNewsRepository.Companion.instance.getNewsListByTime(page, pageSize)
            newsResult.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val freshNewsItems = mutableListOf<FreshNewsItem>()
                        resource.data.forEach { freshNews ->
                            try {
                                // 获取用户信息
                                val userResource = withContext(Dispatchers.IO) {
                                    UserProfileRepository.Companion.instance
                                        .getUserInformationById(freshNews.userId)
                                        .first { it is Resource.Success || it is Resource.Error }
                                }

                                val profile = when (userResource) {
                                    is Resource.Success -> userResource.data
                                    else -> null
                                }

                                val localIsLiked = RefreshNewsCache.getLikeState(freshNews.freshNewsId)
                                val localIsFavorited = RefreshNewsCache.getFavoriteState(freshNews.freshNewsId)
                                val localLikeNum = RefreshNewsCache.getLikeNum(freshNews.freshNewsId)
                                val localFavoriteNum = RefreshNewsCache.getFavoriteNum(freshNews.freshNewsId)

                                var finalLikeNum = freshNews.liked ?: 0
                                if (localLikeNum > finalLikeNum) {
                                    finalLikeNum = localLikeNum
                                } else if (finalLikeNum > localLikeNum) {
                                    RefreshNewsCache.saveLikeNum(freshNews.freshNewsId, finalLikeNum)
                                }

                                var finalFavoriteNum = freshNews.favoritesCount ?: 0
                                if (localFavoriteNum > finalFavoriteNum) {
                                    finalFavoriteNum = localFavoriteNum
                                } else if (finalFavoriteNum > localFavoriteNum) {
                                    RefreshNewsCache.saveFavoriteNum(freshNews.freshNewsId, finalFavoriteNum)
                                }

                                // 构建最终的 FreshNewsItem
                                val freshNewsItem = FreshNewsItem(
                                    freshNewsId = freshNews.freshNewsId,
                                    userId = profile?.userId ?: -1,
                                    authorName = profile?.account ?: "未知用户",
                                    authorAvatar = profile?.avatarUrl ?: "",
                                    title = freshNews.title,
                                    content = freshNews.content,
                                    images = freshNews.images,
                                    tags = freshNews.tags,
                                    liked = finalLikeNum,
                                    createTime = freshNews.createTime,
                                    comments = freshNews.comments,
                                    allowComments = freshNews.allowComments,
                                    favoritesCount = finalFavoriteNum,
                                    location = "北京"
                                ).apply {
                                    isLiked = localIsLiked
                                    isFavorited = localIsFavorited
                                }

                                freshNewsItems.add(freshNewsItem)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Log.e("FreshNews", "Error processing news item: ${e.message}")
                            }
                        }

                        updateState { copy(freshNewsList = Resource.Success(freshNewsItems)) }

                    }
                    is Resource.Error -> {
                        updateState { copy(freshNewsList = Resource.Error(resource.msg)) }
                        handler.post {
                            CustomToast.Companion.showMessage(
                                PlanetApplication.Companion.appContext,
                                "刷新失败: ${resource.msg}"
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun refreshUserProfileByNetwork(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            UserProfileRepository.Companion.instance.getUserInFormationNoCache(userId)
        }
    }

    private fun cleanupTempFiles(files: List<File>) {
        files.forEach { file ->
            if (file.exists()) file.delete()
        }
    }


    // 点赞
    private fun likeNewsItem(freshNewsItem: FreshNewsItem) {
        viewModelScope.launch {
            val newsId = freshNewsItem.freshNewsId
            val currentLikeCount = freshNewsItem.liked ?: 0
            val isCurrentlyLiked = freshNewsItem.isLiked
            val newLikeState = !isCurrentlyLiked

            try {
                // 乐观更新状态
                updateState {
                    val updatedList = (freshNewsList as? Resource.Success)?.data?.map { item ->
                        if (item.freshNewsId == newsId) {
                            item.copy(
                                liked = if (newLikeState) currentLikeCount + 1 else currentLikeCount - 1
                            ).apply {
                                isLiked = newLikeState
                                RefreshNewsCache.saveLikeNum(newsId,liked)
                            }
                        } else {
                            item
                        }
                    }

                    if (updatedList != null) {
                        copy(freshNewsList = Resource.Success(updatedList))
                    } else {
                        this
                    }
                }

                // 保存点赞状态到 MMKV
                RefreshNewsCache.saveLikeState(newsId, newLikeState)

                // 发送网络请求
                withContext(Dispatchers.IO) {
                    Log.d("FreshNewsVM", "发送点赞请求: id=$newsId")
                    val result = FreshNewsRepository.Companion.instance.likeNews(newsId)

                    result.collect { resource ->
                        withContext(Dispatchers.Main) {
                            when (resource) {
                                is Resource.Success -> {
                                    Log.d("FreshNewsVM", "点赞操作成功: id=$newsId")
                                }

                                is Resource.Error -> {
                                    Log.d("FreshNewsVM", "点赞请求失败，回滚状态: id=$newsId")
                                    updateState {
                                        val updatedList =
                                            (freshNewsList as? Resource.Success)?.data?.map { item ->
                                                if (item.freshNewsId == newsId) {
                                                    item.copy(
                                                        liked = currentLikeCount
                                                    ).apply {
                                                        isLiked = isCurrentlyLiked  // 回滚到原始状态
                                                    }
                                                } else {
                                                    item
                                                }
                                            }

                                        if (updatedList != null) {
                                            copy(freshNewsList = Resource.Success(updatedList))
                                        } else {
                                            this
                                        }
                                    }

                                    handler.post {
                                        CustomToast.Companion.showMessage(
                                            PlanetApplication.Companion.appContext,
                                            "操作失败: ${resource.msg}"
                                        )
                                    }
                                }

                                else -> {}
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FreshNewsVM", "点赞操作异常: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    updateState {
                        val updatedList = (freshNewsList as? Resource.Success)?.data?.map { item ->
                            if (item.freshNewsId == newsId) {
                                item.copy(
                                    liked = currentLikeCount
                                ).apply {
                                    isLiked = isCurrentlyLiked
                                    RefreshNewsCache.saveFavoriteNum(newsId, liked)
                                }
                            } else {
                                item
                            }
                        }

                        if (updatedList != null) {
                            copy(freshNewsList = Resource.Success(updatedList))
                        } else {
                            this
                        }
                    }

                    handler.post {
                        CustomToast.Companion.showMessage(
                            PlanetApplication.Companion.appContext,
                            "操作出错: ${e.message}"
                        )
                    }
                }
            }
        }
    }


    private fun favoriteNewsItem(freshNewsItem: FreshNewsItem) {
        viewModelScope.launch {
            // 保存初始状态
            val newsId = freshNewsItem.freshNewsId
            val currentFavoriteCount = freshNewsItem.favoritesCount ?: 0
            val isCurrentlyFavorited = freshNewsItem.isFavorited
            // 计算新的收藏状态
            val newFavoriteState = !isCurrentlyFavorited

            try {
                // 乐观更新状态
                updateState {
                    val updatedList = (freshNewsList as? Resource.Success)?.data?.map { item ->
                        if (item.freshNewsId == newsId) {
                            item.copy(
                                favoritesCount = if (newFavoriteState) currentFavoriteCount + 1 else currentFavoriteCount - 1
                            ).apply {
                                isFavorited = newFavoriteState  // 使用计算好的新状态
                                RefreshNewsCache.saveFavoriteNum(newsId,favoritesCount)
                            }
                        } else {
                            item
                        }
                    }

                    if (updatedList != null) {
                        copy(freshNewsList = Resource.Success(updatedList))
                    } else {
                        this
                    }
                }


                // 保存收藏状态到 MMKV
                RefreshNewsCache.saveFavoriteState(newsId, newFavoriteState)

                // 发送网络请求
                withContext(Dispatchers.IO) {
                    Log.d("FreshNewsVM", "发送收藏请求: id=$newsId")
                    val result = FreshNewsRepository.Companion.instance.favoriteNews(newsId)

                    result.collect { resource ->
                        withContext(Dispatchers.Main) {
                            when (resource) {
                                is Resource.Success -> {
                                    // 请求成功，只显示 Toast，不需要更新状态（因为乐观更新已经处理了）
                                    val actionText =
                                        if (newFavoriteState) "已收藏" else "已取消收藏"
                                    handler.post {
                                        CustomToast.Companion.showMessage(
                                            PlanetApplication.Companion.appContext,
                                            actionText
                                        )
                                    }
                                }

                                is Resource.Error -> {
                                    // 请求失败，回滚状态
                                    Log.d("FreshNewsVM", "收藏请求失败，回滚状态: id=$newsId")
                                    updateState {
                                        val updatedList =
                                            (freshNewsList as? Resource.Success)?.data?.map { item ->
                                                if (item.freshNewsId == newsId) {
                                                    item.copy(
                                                        favoritesCount = currentFavoriteCount
                                                    ).apply {
                                                        isFavorited =
                                                            isCurrentlyFavorited  // 回滚到原始状态
                                                    }
                                                } else {
                                                    item
                                                }
                                            }
                                        if (updatedList != null) {
                                            copy(freshNewsList = Resource.Success(updatedList))
                                        } else {
                                            this
                                        }
                                    }

                                    handler.post {
                                        CustomToast.Companion.showMessage(
                                            PlanetApplication.Companion.appContext,
                                            "操作失败: ${resource.msg}"
                                        )
                                    }
                                }

                                else -> {}
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FreshNewsVM", "收藏操作异常: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    // 发生异常时回滚状态
                    updateState {
                        val updatedList = (freshNewsList as? Resource.Success)?.data?.map { item ->
                            if (item.freshNewsId == newsId) {
                                item.copy(
                                    favoritesCount = currentFavoriteCount
                                ).apply {
                                    isFavorited = isCurrentlyFavorited  // 回滚到原始状态
                                }
                            } else {
                                item
                            }
                        }

                        if (updatedList != null) {
                            copy(freshNewsList = Resource.Success(updatedList))
                        } else {
                            this
                        }
                    }

                    handler.post {
                        CustomToast.Companion.showMessage(
                            PlanetApplication.Companion.appContext,
                            "操作出错: ${e.message}"
                        )
                    }
                }
            }
        }
    }
}