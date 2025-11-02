package com.creamaker.changli_planet_app.freshNews.viewModel

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import com.creamaker.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.mvi.MviViewModel
import com.creamaker.changli_planet_app.core.network.ApiResponse
import com.creamaker.changli_planet_app.freshNews.contract.CommentsContract
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.CommentsCache
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.FreshNews
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.FreshNewsItem
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level1CommentItem
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level1Comments
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level1CommentsResult
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level2CommentsResult
import com.creamaker.changli_planet_app.freshNews.data.remote.repository.CommentsRepository
import com.creamaker.changli_planet_app.widget.view.CustomToast
import com.gradle.scan.agent.serialization.scan.serializer.kryo.it
import kotlinx.coroutines.launch

class CommentsViewModel: MviViewModel<CommentsContract.Intent, CommentsContract.State>() {
    private val TAG = "CommentsViewModel"
    private val handler = Handler(Looper.getMainLooper())

    override fun processIntent(intent: CommentsContract.Intent) {
        when(intent){
            is CommentsContract.Intent.LoadFreshNews-> loadFreshNews(intent.freshNewsItem)
            is CommentsContract.Intent.SendComment-> sendComment(intent.freshNewsId,intent.commentContent,intent.parentId)
            is CommentsContract.Intent.Level1CommentLikedClick -> likeLevel1Comment(intent.level1CommentItem)
            is CommentsContract.Intent.LoadLevel1Comments -> loadLevel1Comments(intent.freshNewsItem,intent.page,intent.pageSize)
        }
    }

    override fun initialState() =  CommentsContract.State(
        FreshNewsItem(
            freshNewsId = -1,
            userId = -1,
            authorName = "",
            authorAvatar = "",
            title = "",
            content = "",
            images = emptyList(),
            tags = emptyList(),
            liked = 0,
            comments = 0,
            createTime = "",
            allowComments = 1,
            favoritesCount = 0,
            location = "",
            isLiked = false,
            isFavorited = false
        ),
        level1CommentsResults = listOf(Level1CommentsResult.Loading),
        level2CommentsResults = listOf(Level2CommentsResult.Loading)
    )

    private fun loadFreshNews(freshNewsItem: FreshNewsItem){
        updateState {
            copy(
                freshNewsItem = freshNewsItem
            )
        }
    }
    private fun loadLevel1Comments(freshNewsItem: FreshNewsItem, page: Int, pageSize: Int) {
        viewModelScope.launch {
            if (!state.value.level1CommentsResults.contains(Level1CommentsResult.Loading) ||
                state.value.level1CommentsResults[0] is Level1CommentsResult.Loading) {
                updateState {
                    copy(
                        level1CommentsResults = state.value.level1CommentsResults.toMutableList()
                            .apply {
                                removeAll{it is Level1CommentsResult.Loading}
                                add(Level1CommentsResult.Loading)
                            }
                    )
                }
                val refreshLikedCountResponse = CommentsRepository.instance.refreshLikeCount()
                refreshLikedCountResponse.collect {
                    when (it) {
                        is ApiResponse.Success -> {
                            Log.d(TAG, "loadLevel1Comments: Successfully refreshed liked counts")
                        }

                        is ApiResponse.Error -> {
                            Log.e(TAG, "loadLevel1Comments: Failed to refresh liked counts: ${it.msg}")
                        }

                        is ApiResponse.Loading -> {}
                    }
                }

                val response = CommentsRepository.instance.loadLevel1Comments(freshNewsItem,page,pageSize,
                    UserInfoManager.userId)
                response.collect { apiResponse ->
                    when (apiResponse) {
                        is ApiResponse.Success -> {
                            val newComments = apiResponse.data?.commentsList

                            val level1CommentsResultList = newComments?.mapIndexed {index, it ->
                                Level1CommentsResult.Success(
                                    comment = Level1CommentItem(
                                        freshNewsId = it.freshNewsId,
                                        commentId = it.commentId,
                                        liked = it.likedCount,
                                        userAvatar = it.userAvatar,
                                        userName = it.userName,
                                        createTime = it.createTime,
                                        userIp = it.commentIp,
                                        content = it.content,
                                        userId = it.userId,
                                        isLiked = apiResponse.data.isLikedList[index] == "true"
                                    )
                                )
                            }?.toList() ?: emptyList()
                            Log.d(TAG, "loadLevel1Comments: Loaded ${newComments?.size?:-1} comments")
                            if (level1CommentsResultList.isEmpty() && page == 1) {
                                updateState {
                                    copy(
                                        level1CommentsResults = listOf(Level1CommentsResult.Empty)
                                    )
                                }
                            } else if (level1CommentsResultList.isEmpty()&& page>1){
                                updateState {
                                    val currentComments =
                                        state.value.level1CommentsResults.toMutableList()
                                    // 移除 Loading 状态
                                    currentComments.removeAll { it is Level1CommentsResult.Loading }
                                    currentComments.add(Level1CommentsResult.noMore)

                                    copy(
                                        level1CommentsResults = currentComments
                                    )
                                }
                            }
                            else if (level1CommentsResultList.size<10){
                                updateState {
                                    val currentComments =
                                        state.value.level1CommentsResults.toMutableList()
                                    // 移除 Loading 状态
                                    currentComments.removeAll { it is Level1CommentsResult.Loading }
                                    currentComments.addAll(level1CommentsResultList)
                                    currentComments.add(Level1CommentsResult.noMore)
                                    copy(
                                        level1CommentsResults = currentComments
                                    )
                                }
                            }
                            else {
                                updateState {
                                    val currentComments =
                                        state.value.level1CommentsResults.toMutableList()
                                    // 移除 Loading 状态
                                    currentComments.removeAll { it is Level1CommentsResult.Loading }
                                    currentComments.addAll(level1CommentsResultList)
                                    copy(
                                        level1CommentsResults = currentComments
                                    )
                                }
                            }
                        }

                        is ApiResponse.Error -> {
                            Log.e(TAG, "loadLevel1Comments: ${apiResponse.msg}")
                            updateState {
                                val currentComments =
                                    state.value.level1CommentsResults.toMutableList()
                                // 移除 Loading 状态
                                currentComments.removeAll { it is Level1CommentsResult.Loading }
                                currentComments.add(Level1CommentsResult.Error)
                                copy(
                                    level1CommentsResults = currentComments
                                )
                            }
                        }

                        is ApiResponse.Loading ->{}
                    }
                }
            }
        }
    }
    private fun sendComment(freshNewsId: Int, commentContent: String, parentId: Int) {
        val comments = state.value.level1CommentsResults
        val last = comments.lastOrNull()
        // 防止重复请求
        if (last is Level1CommentsResult.Loading || last is Level1CommentsResult.Error) return

        viewModelScope.launch {
            // 显示加载状态
            updateState {
                copy(level1CommentsResults = comments + Level1CommentsResult.Loading)
            }
            updateState {
                copy(
                    level1CommentPostState = 1 // 发布中
                )
            }
            val response = CommentsRepository.instance.sendComment(
                freshNewsId,
                UserInfoManager.userId,
                commentContent,
                parentId
            )

            // 如果是一级评论，先临时插入本地评论到顶部
            if (parentId == 0) {
                val tempComment = Level1CommentItem(
                    freshNewsId = freshNewsId,
                    commentId = -1,
                    liked = 0,
                    userAvatar = UserInfoManager.userAvatar,
                    userName = UserInfoManager.username,
                    createTime = "刚刚",
                    userIp = CommentsCache.getIp(),
                    content = commentContent,
                    userId = UserInfoManager.userId,
                    level2CommentsCount = 0,
                    isLiked = false
                )
                if (comments.isNotEmpty() && comments[0] is Level1CommentsResult.Empty) {
                    // 如果当前是空状态，替换为空评论
                    updateState {
                        copy(level1CommentsResults = listOf(Level1CommentsResult.Success(tempComment)))
                    }
                } else {
                    updateState {
                        copy(level1CommentsResults = listOf(Level1CommentsResult.Success(tempComment)) + comments)
                    }
                }

            }
            else{
                //TODO: 二级评论的本地插入逻辑
            }

            response.collect { apiResponse ->
                when (apiResponse) {
                    is ApiResponse.Success -> {
                        showToast("评论发布成功")

                        if (parentId == 0) {
                            updateState {
                                copy(
                                    level1CommentsResults = level1CommentsResults.map {
                                        if (it is Level1CommentsResult.Success &&
                                            it.comment.commentId == -1 &&
                                            it.comment.content == commentContent
                                        ) {
                                            it.copy(
                                                comment = it.comment.copy(commentId = apiResponse.data ?: -1)
                                            )
                                        } else it
                                    },
                                    level1CommentPostState = 2 // 发布成功
                                )

                            }
                        } else {
                            // TODO: 二级评论处理逻辑
                        }
                    }

                    is ApiResponse.Error -> {
                        showToast("评论发布失败")

                        if (parentId == 0) {
                            updateState {
                                copy(
                                    level1CommentsResults = level1CommentsResults.filterNot {
                                        it is Level1CommentsResult.Success &&
                                                it.comment.commentId == -1 &&
                                                it.comment.content == commentContent
                                    },
                                    level1CommentPostState = 3 // 发布失败
                                )
                            }
                        } else {
                            // TODO: 二级评论回滚处理
                        }
                    }

                    is ApiResponse.Loading -> {
                        // 此分支可省略，因为已在 launch 开始时设置 Loading
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        handler.post {
            CustomToast.showMessage(PlanetApplication.appContext, message)
        }
    }
    private fun likeLevel1Comment(level1CommentItem: Level1CommentItem) {
        viewModelScope.launch {
            val previousState = level1CommentItem.isLiked
            val previousLikedCount = level1CommentItem.liked
            val newLikedState = !previousState
            val newLikedCount = if (newLikedState) previousLikedCount + 1 else (previousLikedCount - 1)
            val response = CommentsRepository.instance.likeComment(
                commentId = level1CommentItem.commentId,
                userId = UserInfoManager.userId,
                isParent = 1
            )
            response.collect { apiResponse ->
                when (apiResponse) {
                    is ApiResponse.Success -> {
                        updateState {
                            copy(
                                level1CommentsResults = level1CommentsResults.map {
                                    if (it is Level1CommentsResult.Success &&
                                        it.comment.commentId == level1CommentItem.commentId
                                    ) {
                                        it.copy(
                                            comment = it.comment.copy(
                                                liked = newLikedCount,
                                                isLiked = newLikedState
                                            )
                                        )
                                    } else it
                                }
                            )
                        }
                    }

                    is ApiResponse.Error -> {
                        showToast("点赞失败: ${apiResponse.msg}")
                    }

                    is ApiResponse.Loading -> {}
                }
            }
        }
    }

    override fun onCleared() {
        handler.removeCallbacksAndMessages(null)
        super.onCleared()
    }
}