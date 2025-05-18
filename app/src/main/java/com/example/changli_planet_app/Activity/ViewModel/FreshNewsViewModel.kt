package com.example.changli_planet_app.Activity.ViewModel

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.viewModelScope

import com.example.changli_planet_app.Activity.Contract.FreshNewsContract
import com.example.changli_planet_app.Cache.UserInfoManager
import com.example.changli_planet_app.Core.CoroutineContext.ErrorCoroutineContext
import com.example.changli_planet_app.Core.MVI.MviViewModel
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Data.jsonbean.UserDisplayInformation
import com.example.changli_planet_app.Network.Resource
import com.example.changli_planet_app.Network.Response.FreshNews
import com.example.changli_planet_app.Network.Response.FreshNewsItem
import com.example.changli_planet_app.Network.Response.FreshNews_Publish
import com.example.changli_planet_app.Network.repository.FreshNewsRepository
import com.example.changli_planet_app.Network.repository.UserProfileRepository
import com.example.changli_planet_app.Widget.View.CustomToast
import com.example.changli_planet_app.Network.Response.UserProfile
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
        FreshNews_Publish(),
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
            val result = FreshNewsRepository.instance.postFreshNews(
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
                            CustomToast.showMessage(
                                PlanetApplication.appContext,
                                "发布成功"
                            )
                        }
                    }

                    else -> {
                        handler.post {
                            CustomToast.showMessage(
                                PlanetApplication.appContext,
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
                publishNews = FreshNews_Publish()
            )
        }
    }

    private fun refreshNewsByTime(page: Int, pageSize: Int) {
        viewModelScope.launch {
            updateState { copy(freshNewsList = Resource.Loading()) }

            // 保存当前的收藏状态
            val currentList = (state.value.freshNewsList as? Resource.Success)?.data
            val favoriteStates = currentList?.associate {
                it.freshNewsId to it.isFavorited
            } ?: emptyMap()

            val newsResult = FreshNewsRepository.instance.getNewsListByTime(page, pageSize)
            newsResult.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                    }

                    is Resource.Success -> {
                        val freshNewsItems = mutableListOf<FreshNewsItem>()
                        resource.data.forEach { freshNews ->
                            try {
                                val userResource = withContext(Dispatchers.IO) {
                                    UserProfileRepository.instance
                                        .getUserInformationById(freshNews.userId)
                                        .first { it is Resource.Success || it is Resource.Error }
                                }

                                val profile = when (userResource) {
                                    is Resource.Success -> userResource.data
                                    else -> null
                                }

                                val freshNewsItem = FreshNewsItem(
                                    freshNewsId = freshNews.freshNewsId,
                                    userId = profile?.userId ?: -1,
                                    authorName = profile?.account ?: "未知用户",
                                    authorAvatar = profile?.avatarUrl ?: "",
                                    title = freshNews.title,
                                    content = freshNews.content,
                                    images = freshNews.images,
                                    tags = freshNews.tags,
                                    liked = freshNews.liked,
                                    createTime = freshNews.createTime,
                                    comments = freshNews.comments,
                                    allowComments = freshNews.allowComments,
                                    favoritesCount = freshNews.favoritesCount,
                                    location = "北京"
                                ).apply {
                                    // 恢复之前的收藏状态
                                    isFavorited = favoriteStates[freshNewsId] ?: false
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
                            CustomToast.showMessage(
                                PlanetApplication.appContext,
                                "刷新失败: ${
                                    resource.msg
                                }"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun refreshUserProfileByNetwork(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            UserProfileRepository.instance.getUserInFormationNoCache(userId)
        }
    }

    private fun cleanupTempFiles(files: List<File>) {
        files.forEach { file ->
            if (file.exists()) file.delete()
        }
    }


    // 点赞 -
    private fun likeNewsItem(freshNewsItem: FreshNewsItem) {
        viewModelScope.launch {
            try {
                // 获取当前状态
                val currentLikeCount = freshNewsItem.liked ?: 0
                val isCurrentlyLiked = freshNewsItem.isLiked
                // 发送网络请求(网络操作在IO线程)
                withContext(Dispatchers.IO) {
                    Log.d("FreshNewsVM", "发送网络请求: id=${freshNewsItem.freshNewsId}")
                    val result = FreshNewsRepository.instance.likeNews(freshNewsItem.freshNewsId)

                    result.collect { resource ->
                        withContext(Dispatchers.Main) {
                            when (resource) {
                                is Resource.Success -> {
                                }
                                is Resource.Error -> {
                                    // 请求失败，回滚UI状态
                                    Log.d("FreshNewsVM", "请求失败，回滚UI: id=${freshNewsItem.freshNewsId}")
                                    updateNewsItemInList(freshNewsItem.freshNewsId) { item ->
                                        val updated = item.copy(
                                            liked = currentLikeCount
                                        )
                                        updated.isLiked = isCurrentlyLiked
                                        updated
                                    }

                                    CustomToast.showMessage(
                                        PlanetApplication.appContext,
                                        "操作失败: ${resource.msg}"
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FreshNewsVM", "点赞操作异常: ${e.message}", e)
                // 确保异常处理也在主线程
                withContext(Dispatchers.Main) {
                    CustomToast.showMessage(
                        PlanetApplication.appContext,
                        "操作出错: ${e.message}"
                    )
                }
            }
        }
    }

    // 收藏
    private fun favoriteNewsItem(freshNewsItem: FreshNewsItem) {
        viewModelScope.launch {
            val currentFavoriteCount = freshNewsItem.favoritesCount ?: 0
            val isCurrentlyFavorited = freshNewsItem.isFavorited

            // 计算新状态
            val newFavoriteCount = if (isCurrentlyFavorited) currentFavoriteCount - 1 else currentFavoriteCount + 1
            val newFavoriteState = !isCurrentlyFavorited

            // 立即更新UI (乐观更新)
            updateNewsItemInList(freshNewsItem.freshNewsId) { item ->
                item.copy(
                    favoritesCount = newFavoriteCount,
                    isFavorited = newFavoriteState
                )
            }

            val result = FreshNewsRepository.instance.favoriteNews(freshNewsItem.freshNewsId)

            result.onEach { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val actionText = if (newFavoriteState) "已收藏" else "已取消收藏"
                        handler.post {
                            CustomToast.showMessage(
                                PlanetApplication.appContext,
                                actionText
                            )
                        }
                    }
                    is Resource.Error -> {
                        updateNewsItemInList(freshNewsItem.freshNewsId) { item ->
                            item.copy(
                                favoritesCount = currentFavoriteCount,
                                isFavorited = isCurrentlyFavorited
                            )
                        }
                        handler.post {
                            CustomToast.showMessage(
                                PlanetApplication.appContext,
                                "操作失败: ${resource.msg}"
                            )
                        }
                    }
                    else -> {}
                }
            }.launchIn(viewModelScope)
        }
    }
    private fun updateNewsItemInList(
        freshNewsId: Int,
        updateFunction: (FreshNewsItem) -> FreshNewsItem
    ) {
        val currentState = state.value
        val freshNewsList =
            (currentState.freshNewsList as? Resource.Success)?.data?.toMutableList() ?: return

        val index = freshNewsList.indexOfFirst { it.freshNewsId == freshNewsId }
        if (index != -1) {
            val item = freshNewsList[index]
            freshNewsList[index] = updateFunction(item)

            updateState {
                copy(freshNewsList = Resource.Success(freshNewsList))
            }
        }
    }
}