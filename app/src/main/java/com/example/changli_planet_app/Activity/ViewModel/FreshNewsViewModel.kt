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
                                    favoritesCount = freshNews.favoritesCount
                                )

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
}