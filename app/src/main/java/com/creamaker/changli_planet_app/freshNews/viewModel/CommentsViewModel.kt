package com.creamaker.changli_planet_app.freshNews.viewModel

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.mvi.MviViewModel
import com.creamaker.changli_planet_app.core.network.ApiResponse
import com.creamaker.changli_planet_app.freshNews.contract.CommentsContract
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.CommentsCache
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.CommentsResult
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.FreshNewsItem
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level1CommentItem
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.Level2CommentItem
import com.creamaker.changli_planet_app.freshNews.data.remote.repository.CommentsRepository
import com.creamaker.changli_planet_app.widget.view.CustomToast
import kotlinx.coroutines.launch

class CommentsViewModel: MviViewModel<CommentsContract.Intent, CommentsContract.State>() {
    private val TAG = "CommentsViewModel"
    private val handler = Handler(Looper.getMainLooper())

    override fun processIntent(intent: CommentsContract.Intent) {
        when (intent) {
            is CommentsContract.Intent.LoadFreshNews -> loadFreshNews(intent.freshNewsItem)
            is CommentsContract.Intent.SendComment -> sendComment(
                intent.freshNewsId,
                intent.commentContent,
                intent.parentId
            )

            is CommentsContract.Intent.Level1CommentLikedClick -> likeLevel1Comment(
                intent.level1CommentItem,
                intent.isInLevel2CommentsPage
            )

            is CommentsContract.Intent.LoadLevel1Comments -> loadLevel1Comments(
                intent.freshNewsItem,
                intent.page,
                intent.pageSize
            )

            is CommentsContract.Intent.Level2CommentLikedClick -> likeLevel2Comment(intent.level2CommentItem)
            is CommentsContract.Intent.LoadLevel1Comment -> loadLevel1Comment(intent.level1CommentItem)
            is CommentsContract.Intent.LoadLevel2Comments -> loadLevel2Comments(
                intent.level1CommentItem,
                intent.page,
                intent.pageSize,
                state.value.freshNewsItem.freshNewsId
            )

            CommentsContract.Intent.ResetLevel2Comments -> resetLevel2Comments()
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
        level1CommentsResults = listOf(CommentsResult.Loading),
        level2CommentsResults = listOf(CommentsResult.Loading),
        level1CommentPostState = 0
    )

    private fun loadFreshNews(freshNewsItem: FreshNewsItem){
        updateState {
            copy(freshNewsItem = freshNewsItem)
        }
    }
    private fun loadLevel1Comments(freshNewsItem: FreshNewsItem, page: Int, pageSize: Int) {
        viewModelScope.launch {
            if (!state.value.level1CommentsResults.contains(CommentsResult.Loading) ||
                state.value.level1CommentsResults[0] is CommentsResult.Loading) {
                updateState {
                    copy(
                        level1CommentsResults = state.value.level1CommentsResults.toMutableList()
                            .apply {
                                removeAll{it is CommentsResult.Loading}
                                add(CommentsResult.Loading)
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
                                CommentsResult.Success.Level1CommentsSuccess(
                                    level1Comment = Level1CommentItem(
                                        freshNewsId = it.freshNewsId,
                                        commentId = it.commentId,
                                        liked = it.likedCount,
                                        userAvatar = it.userAvatar,
                                        userName = it.userName,
                                        createTime = it.createTime,
                                        userIp = it.commentIp,
                                        content = it.content,
                                        userId = it.userId,
                                        isLiked = apiResponse.data.isLikedList[index] == "true",
                                        level2CommentsCount = it.childCount
                                    )
                                )
                            }?.toList() ?: emptyList()
                            Log.d(TAG, "loadLevel1Comments: Loaded ${newComments?.size?:-1} comments")
                            if (level1CommentsResultList.isEmpty() && page == 1) {
                                updateState {
                                    copy(
                                        level1CommentsResults = listOf(CommentsResult.Empty)
                                    )
                                }
                            } else if (level1CommentsResultList.isEmpty()&& page>1){
                                updateState {
                                    val currentComments =
                                        state.value.level1CommentsResults.toMutableList()
                                    // 移除 Loading 状态
                                    currentComments.removeAll { it is CommentsResult.Loading }
                                    currentComments.add(CommentsResult.noMore)

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
                                    currentComments.removeAll { it is CommentsResult.Loading }
                                    currentComments.addAll(level1CommentsResultList)
                                    currentComments.add(CommentsResult.noMore)
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
                                    currentComments.removeAll { it is CommentsResult.Loading }
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
                                currentComments.removeAll { it is CommentsResult.Loading }
                                currentComments.add(CommentsResult.Error)
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
        val level1Comments = state.value.level1CommentsResults
        val level2Comments = state.value.level2CommentsResults
        val last1 = level1Comments.lastOrNull()
        val last2 = level2Comments.lastOrNull()
        // 防止重复请求
        if (parentId == 0){
            if(last1 is CommentsResult.Loading || last1 is CommentsResult.Error)return
        }
        else{
            if ((last2 is CommentsResult.Loading && level2Comments.size != 1) ||
                last2 is CommentsResult.Error
            )return
        }
        viewModelScope.launch {
            // 显示加载状态
            updateState {
                copy(
                    level1CommentsResults = level1Comments + CommentsResult.Loading,
                    level2CommentsResults = level2Comments + CommentsResult.Loading
                )
            }
            updateState {
                copy(
                    level1CommentPostState = 1, // 发布中
                    level2CommentPostState = 1 // 发布中
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
                if (level1Comments.isNotEmpty() && level1Comments[0] is CommentsResult.Empty) {
                    // 如果当前是空状态，替换为空评论
                    updateState {
                        copy(
                            level1CommentsResults = listOf(
                                CommentsResult.Success.Level1CommentsSuccess(
                                    tempComment
                                )
                            )
                        )
                    }
                } else {
                    updateState {
                        copy(
                            level1CommentsResults = listOf(
                                CommentsResult.Success.Level1CommentsSuccess(
                                    tempComment
                                )
                            ) + level1Comments
                        )
                    }
                }

            }
            else{
                //添加二级评论的情况
                var level1Comment = level1Comments.filterIsInstance<CommentsResult.Success.Level1CommentsSuccess>()
                    .find { it.level1Comment.commentId == parentId }?.level1Comment
                level1Comment = level1Comment?.copy(
                    level2CommentsCount = level1Comment.level2CommentsCount + 1
                )
                updateState {
                    copy(
                        level1CommentsResults = level1Comments.map {
                            if (it is CommentsResult.Success.Level1CommentsSuccess &&
                                it.level1Comment.commentId == parentId
                            ) {
                                it.copy(
                                    level1Comment = level1Comment!!
                                )
                            } else it
                        }
                    )
                }
                val tempLevel2Comment = Level2CommentItem(
                    parentCommentId = parentId,
                    freshNewsId = freshNewsId,
                    liked = 0,
                    userAvatar = UserInfoManager.userAvatar,
                    userName = UserInfoManager.username,
                    createTime = "刚刚",
                    userIp = CommentsCache.getIp(),
                    content = commentContent,
                    userId = UserInfoManager.userId,
                    commentId = -1,
                    isLiked = false
                )
                updateState {
                    val newList = level2Comments.toMutableList()
                    val item = CommentsResult.Success.Level2CommentsSuccess(tempLevel2Comment)
                    if (newList.isEmpty()) {
                        newList.add(item)
                    } else {
                        // 插入到第二个位置（索引 1）
                        val insertIndex = 1
                        newList.add(insertIndex, item)
                    }
                    copy(
                        level2CommentsResults = newList
                    )
                }
            }

            response.collect { apiResponse ->
                when (apiResponse) {
                    is ApiResponse.Success -> {
                        showToast("评论发布成功")

                        if (parentId == 0) {
                            updateState {
                                copy(
                                    level1CommentsResults = level1CommentsResults.map {

                                        if (it is CommentsResult.Success.Level1CommentsSuccess &&
                                            it.level1Comment.commentId == -1 &&
                                            it.level1Comment.content == commentContent
                                        ) {
                                            it.copy(
                                                level1Comment = it.level1Comment.copy(
                                                    commentId = apiResponse.data ?: -1
                                                )
                                            )
                                        } else it
                                    },
                                    level1CommentPostState = 2 // 发布成功
                                )

                            }
                        } else {
                            //更新二级评论中的临时评论ID
                            updateState {
                                copy(
                                    level2CommentsResults = level2CommentsResults.map {
                                        if (it is CommentsResult.Success.Level2CommentsSuccess &&
                                            it.level2Comment.commentId == -1 &&
                                            it.level2Comment.content == commentContent
                                        ) {
                                            it.copy(
                                                level2Comment = it.level2Comment.copy(
                                                    commentId = apiResponse.data ?: -1
                                                )
                                            )
                                        } else it
                                    },
                                    level2CommentPostState = 2 // 发布成功
                                )
                            }
                        }
                    }

                    is ApiResponse.Error -> {
                        showToast("评论发布失败")

                        if (parentId == 0) {
                            updateState {
                                copy(
                                    level1CommentsResults = level1CommentsResults.filterNot {
                                        it is CommentsResult.Success.Level1CommentsSuccess &&
                                                it.level1Comment.commentId == -1 &&
                                                it.level1Comment.content == commentContent
                                    },
                                    level1CommentPostState = 3 // 发布失败
                                )
                            }
                        } else {
                            //回滚二级评论数量
                            showToast("评论发布失败")
                            var level1Comment = level1Comments.filterIsInstance<CommentsResult.Success.Level1CommentsSuccess>()
                                .find { it.level1Comment.commentId == parentId }?.level1Comment
                            level1Comment = level1Comment?.copy(
                                level2CommentsCount = level1Comment.level2CommentsCount - 1
                            )
                            updateState {
                                copy(
                                    level1CommentsResults = level1CommentsResults.map {
                                        if (it is CommentsResult.Success.Level1CommentsSuccess &&
                                            it.level1Comment.commentId == parentId
                                        ) {
                                            it.copy(
                                                level1Comment = level1Comment!!
                                            )
                                        } else it
                                    },
                                    level2CommentPostState = 3 // 发布失败
                                )
                            }
                            //移除刚添加的二级评论
                            updateState {
                                copy(
                                    level2CommentsResults = level2CommentsResults.filterNot {
                                        it is CommentsResult.Success.Level2CommentsSuccess &&
                                                it.level2Comment.commentId == -1 &&
                                                it.level2Comment.content == commentContent
                                    }
                                )
                            }
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
    private fun likeLevel1Comment(level1CommentItem: Level1CommentItem,isInLevel2CommentsPage:Boolean=false) {
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
                                    if (it is CommentsResult.Success.Level1CommentsSuccess &&
                                        it.level1Comment.commentId == level1CommentItem.commentId
                                    ) {
                                        it.copy(
                                            level1Comment = it.level1Comment.copy(
                                                liked = newLikedCount,
                                                isLiked = newLikedState
                                            )
                                        )
                                    } else it
                                }
                            )
                        }
                        if (isInLevel2CommentsPage) {
                            updateState {
                                copy(
                                    level2CommentsResults = level2CommentsResults.map {
                                        if (it is CommentsResult.Success.Level1CommentsSuccess &&
                                            it.level1Comment.commentId == level1CommentItem.commentId
                                        ) {
                                            it.copy(
                                                level1Comment = it.level1Comment.copy(
                                                    liked = newLikedCount,
                                                    isLiked = newLikedState
                                                )
                                            )
                                        } else it
                                    }
                                )
                            }
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
    private fun loadLevel1Comment(level1CommentItem: Level1CommentItem){
        updateState {
            copy(
                level2CommentsResults = listOf(CommentsResult.Success.Level1CommentsSuccess(level1CommentItem))
            )
        }
    }
    private fun loadLevel2Comments(
        level1CommentItem: Level1CommentItem,
        page: Int,
        pageSize: Int,
        freshNewsId: Int
    ) {
        if (
            state.value.level2CommentsResults.contains(CommentsResult.Loading) &&
            state.value.level2CommentsResults.size != 1
        )return

        viewModelScope.launch {
            updateState {
                copy(
                    level2CommentsResults = state.value.level2CommentsResults.toMutableList()
                        .apply {
                            removeAll{it is CommentsResult.Loading}
                            add(CommentsResult.Loading)
                        }
                )
            }
            val response = CommentsRepository.instance.loadLevel2Comments(
                freshNewsId,
                level1CommentItem.commentId,
                page,
                pageSize,
                UserInfoManager.userId
            )
            response.collect { apiResponse ->
                when(apiResponse){
                    is ApiResponse.Success -> {
                        val newComments = apiResponse.data?.commentsList
                        val likedList = apiResponse.data?.isLikedList
                        val level2CommentsResultList = newComments?.mapIndexed {index, it ->
                            CommentsResult.Success.Level2CommentsSuccess(
                                level2Comment = Level2CommentItem(
                                    freshNewsId = it.freshNewsId,
                                    commentId = it.commentId,
                                    liked = it.likedCount,
                                    userAvatar = it.userAvatar,
                                    userName = it.userName,
                                    createTime = it.createTime,
                                    userIp = it.commentIp,
                                    content = it.content,
                                    userId = it.userId,
                                    parentCommentId = it.parentCommentId,
                                    isLiked = likedList?.get(index) == "true"
                                )
                            )
                        }?.toList() ?: emptyList()
                        if (level2CommentsResultList.isEmpty()&& page>1){
                            updateState {
                                val currentComments =
                                    state.value.level2CommentsResults.toMutableList()
                                // 移除 Loading 状态
                                currentComments.removeAll { it is CommentsResult.Loading }
                                currentComments.add(CommentsResult.noMore)

                                copy(
                                    level2CommentsResults = currentComments
                                )
                            }
                        }
                        else if (level2CommentsResultList.size<10){
                            updateState {
                                val currentComments = state.value.level2CommentsResults.toMutableList()
                                // 移除 Loading 状态和已有的一级评论，避免重复
                                currentComments.removeAll { it is CommentsResult.Loading } // 追加二级评论与 noMore
                                currentComments.addAll(level2CommentsResultList)
                                currentComments.add(CommentsResult.noMore)

                                copy(
                                    level2CommentsResults = currentComments
                                )
                            }
                        }
                        else {
                            updateState {
                                val currentComments =
                                    state.value.level2CommentsResults.toMutableList()
                                // 移除 Loading 状态
                                currentComments.removeAll { it is CommentsResult.Loading }
                                currentComments.addAll(level2CommentsResultList)
                                copy(
                                    level2CommentsResults = currentComments
                                )
                            }
                        }
                    }
                    is ApiResponse.Loading -> { }
                    is ApiResponse.Error -> {
                        Log.e(TAG, "loadLevel2Comments: ${apiResponse.msg}")
                        updateState {
                            val currentComments =
                                state.value.level2CommentsResults.toMutableList()
                            // 移除 Loading 状态
                            currentComments.removeAll { it is CommentsResult.Loading }
                            currentComments.add(CommentsResult.Error)
                            copy(
                                level2CommentsResults = currentComments
                            )
                        }
                    }
                }

            }
        }
    }
    private fun resetLevel2Comments(){
        updateState {
            copy(
                level2CommentsResults = listOf(CommentsResult.Loading),
                level2CommentPostState = 0
            )
        }
    }
    private fun likeLevel2Comment(level2CommentItem: Level2CommentItem) {
        viewModelScope.launch {
            val previousState = level2CommentItem.isLiked
            val previousLikedCount = level2CommentItem.liked
            val newLikedState = !previousState
            val newLikedCount = if (newLikedState) previousLikedCount + 1 else (previousLikedCount - 1)
            val response = CommentsRepository.instance.likeComment(
                commentId = level2CommentItem.commentId,
                userId = UserInfoManager.userId,
                isParent = 0
            )
            response.collect { apiResponse ->
                when (apiResponse) {
                    is ApiResponse.Success -> {
                        updateState {
                            copy(
                                level2CommentsResults = level2CommentsResults.map {
                                    if (it is CommentsResult.Success.Level2CommentsSuccess &&
                                        it.level2Comment.commentId == level2CommentItem.commentId
                                    ) {
                                        it.copy(
                                            level2Comment = it.level2Comment.copy(
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