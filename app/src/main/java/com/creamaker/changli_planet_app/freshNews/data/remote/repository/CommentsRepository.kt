package com.creamaker.changli_planet_app.freshNews.data.remote.repository

import android.util.Log
import com.creamaker.changli_planet_app.core.network.ApiResponse
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.CommentsCache
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.FreshNewsItem
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.LevelCommentRequest
import com.creamaker.changli_planet_app.freshNews.data.remote.api.CommentsApi
import com.creamaker.changli_planet_app.freshNews.data.remote.api.IpService
import com.creamaker.changli_planet_app.utils.RetrofitUtils
import com.gradle.scan.agent.serialization.scan.serializer.kryo.ip
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlin.jvm.java

class CommentsRepository private constructor() {
    companion object{
        val instance by lazy { CommentsRepository() }
    }
    private val TAG = "CommentsRepository"
    private val service = lazy { RetrofitUtils.instanceComments.create(CommentsApi::class.java) }
    private val ipService = lazy { RetrofitUtils.instanceIP.create(IpService::class.java) }
    fun loadLevel1Comments(freshNewsItem: FreshNewsItem,page:Int,pageSize:Int) = flow{
//            Log.d(TAG, "loadLevel1Comments: loading level 1 comments for freshNewsId ${freshNewsItem.freshNewsId}, page $page, pageSize $pageSize")
            val response = service.value.getLevel1Comments(freshNewsItem.freshNewsId,page,pageSize)
            if (response.code == "200"){
                Log.d(TAG, "loadLevel1Comments: success")
                emit(ApiResponse.Success(response.data))
            }
            else{
                Log.d(TAG, "loadLevel1Comments: failed with code ${response.code}")
                emit(ApiResponse.Error(response.msg))
            }
        }.catch {e->
            Log.e(TAG, "loadLevel1Comments: exception ${e.message}",e)
            emit(ApiResponse.Error(e.message ?: "Unknown error"))
    }
    fun sendComment(freshNewsId:Int,userId:Int,content: String,parentId:Int) = flow{
        //获取ip
        var ip: String = "未知"
        val localIp = CommentsCache.getIp()
        if (localIp == "未知") {
            //从网络获取Ip
            val ipResponse = ipService.value.getLocation()
            if (ipResponse.status == "success") {
                CommentsCache.saveIp(ipResponse.regionName ?: "未知")
                ip = ipResponse.regionName ?: "未知"
            }
        } else {
            ip = localIp
        }
        //发布一级评论
        if (parentId == 0) {
            service.value.addLevel1Comment(LevelCommentRequest(
                freshNewsId = freshNewsId,
                userId = userId,
                content = content,
                parentCommentId = 0,
                commentIp = ip
            )).let {response->
                if (response.code == "200"){
                    Log.d(TAG, "sendComment: success")
                    emit(ApiResponse.Success(response.data))
                }
                else{
                    Log.d(TAG, "sendComment: failed with code ${response.code}")
                    emit(ApiResponse.Error(response.msg))
                }
            }
        }
        else{
            //发布二级评论
            service.value.addLevel2Comment(LevelCommentRequest(
                freshNewsId = freshNewsId,
                userId = userId,
                content = content,
                parentCommentId = parentId,
                commentIp = ip
            )).let {response->
                if (response.code == "200"){
                    Log.d(TAG, "sendComment: success")
                    emit(ApiResponse.Success(response.data))
                }
                else{
                    Log.d(TAG, "sendComment: failed with code ${response.code}")
                    emit(ApiResponse.Error(response.msg))
                }
            }
        }
    }.catch {e->
        Log.e(TAG, "sendComment: exception ${e.message}",e)
        emit(ApiResponse.Error(e.message ?: "Unknown error"))
    }
    fun likeComment(commentId:Int,userId: Int,isParent:Int) = flow{
        val response = service.value.likeComment(commentId,userId,isParent)
        if (response.code == "200"){
            Log.d(TAG, "likeComment: success")
            emit(ApiResponse.Success(true))
        }
        else{
            Log.d(TAG, "likeComment: failed with code ${response.code}")
            emit(ApiResponse.Error(response.msg))
        }
    }.catch {e->
        Log.e(TAG, "likeComment: exception ${e.message}",e)
        emit(ApiResponse.Error(e.message ?: "Unknown error"))
    }
    fun refreshLikeCount() = flow{
        val response = service.value.refreshLikeCount()
        if (response.code == "200"){
            Log.d(TAG, "refreshLikeCount: success")
            emit(ApiResponse.Success(true))
        }
        else{
            Log.d(TAG, "refreshLikeCount: failed with code ${response.code}")
            emit(ApiResponse.Error(response.msg))
        }
    }.catch {e->
        Log.e(TAG, "refreshLikeCount: exception ${e.message}",e)
        emit(ApiResponse.Error(e.message ?: "Unknown error"))
    }

}