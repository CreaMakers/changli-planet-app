package com.creamaker.changli_planet_app.feature.mooc.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.core.network.ApiResponse
import com.creamaker.changli_planet_app.feature.mooc.data.remote.dto.MoocHomework
import com.creamaker.changli_planet_app.feature.mooc.data.remote.dto.PendingAssignmentCourse
import com.creamaker.changli_planet_app.feature.mooc.data.remote.repository.MoocRepository
import com.creamaker.changli_planet_app.utils.ResourceUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MoocViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "MoocViewModel"
    }

    private val _isSuccessLogin = MutableStateFlow<ApiResponse<Boolean>>(ApiResponse.Loading())
    val isSuccessLogin = _isSuccessLogin.asStateFlow()

    private val _pendingCourse =
        MutableStateFlow<ApiResponse<List<PendingAssignmentCourse>>>(ApiResponse.Loading())
    val pendingCourse = _pendingCourse.asStateFlow()
    private val _pendingHomeworksByCourse = MutableStateFlow<Map<String, ApiResponse<List<MoocHomework>>>>(mapOf())
    val pendingHomeworksByCourse = _pendingHomeworksByCourse.asStateFlow()
    
    private val _expandedCourseIds = MutableStateFlow<Set<String>>(setOf())
    val expandedCourseIds = _expandedCourseIds.asStateFlow()
    
    private val _preloadedCourseIds = MutableStateFlow<Set<String>>(setOf())
    val preloadedCourseIds = _preloadedCourseIds.asStateFlow()
    // 新增：每个作业是否一天内到期的状态 map（key = homeworkId）
    private val _isDueSoonMap = MutableStateFlow<Map<String, ApiResponse<Boolean>>>(mapOf())
    val isDueSoonMap = _isDueSoonMap.asStateFlow()
    fun toggleCourseExpanded(courseId: String) {
        _expandedCourseIds.value = if (_expandedCourseIds.value.contains(courseId)) {
            _expandedCourseIds.value.minus(courseId)
        } else {
            _expandedCourseIds.value.plus(courseId)
        }
    }
    
    fun markCourseAsPreloaded(courseId: String) {
        _preloadedCourseIds.value = _preloadedCourseIds.value.plus(courseId)
    }
    
    fun isCourseExpanded(courseId: String): Boolean {
        return _expandedCourseIds.value.contains(courseId)
    }
    
    fun isCoursePreloaded(courseId: String): Boolean {
        return _preloadedCourseIds.value.contains(courseId)
    }
    private val repository by lazy { MoocRepository.instance }

    fun login(account: String, password: String) {
        val loginResult = repository.login(account, password)
        loginResult.onEach {
            _isSuccessLogin.value = it
        }.launchIn(viewModelScope)
    }

    fun loginAndFetchCourses(account: String, password: String) {
        viewModelScope.launch {
            try {
                if (account.isEmpty() || password.isEmpty()) {
                    _pendingCourse.value =
                        ApiResponse.Error(ResourceUtil.getStringRes(R.string.school_account_not_bound_cannot_query))
                }
                _isSuccessLogin.value = ApiResponse.Loading()
                _pendingCourse.value = ApiResponse.Loading()
                val loginResult = repository.login(account, password)
                    .filter { it !is ApiResponse.Loading }
                    .first()
                _isSuccessLogin.value = loginResult

                if (loginResult is ApiResponse.Success && loginResult.data) {
                    val courseResult = repository.getCourseNamesWithPendingHomeworks()
                        .filter { it !is ApiResponse.Loading }
                        .first()
                    Log.d(TAG,courseResult.toString())
                    _pendingCourse.value = courseResult
                } else {
                    _pendingCourse.value = ApiResponse.Error((loginResult as ApiResponse.Error).msg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login and fetch courses failed", e)
                _isSuccessLogin.value =
                    ApiResponse.Error(e.message ?: ResourceUtil.getStringRes(R.string.error_unknown))
                _pendingCourse.value =
                    ApiResponse.Error(e.message ?: ResourceUtil.getStringRes(R.string.error_unknown))
            }
        }
    }
    fun getCourseHomeworks(courseId: String){
        viewModelScope.launch {
            try {
                // Set loading state for this specific course
                val currentMap = _pendingHomeworksByCourse.value
                _pendingHomeworksByCourse.value = currentMap.toMutableMap().apply {
                    this[courseId] = ApiResponse.Loading()
                }

                val result = repository.getCourseHomeworks(courseId)
                    .filter { it !is ApiResponse.Loading }
                    .first()

                // Update the map with the result for this specific course
                val updatedMap = _pendingHomeworksByCourse.value.toMutableMap()
                updatedMap[courseId] = result
                _pendingHomeworksByCourse.value = updatedMap

                // 如果获取成功，则为每个作业触发 isDueSoon 检查并更新 ViewModel 中的状态
                if (result is ApiResponse.Success) {
                    result.data.forEach { hw ->
                        // 只有在还没有检查或者之前是错误/已过时情况下才重新检查
                        val existing = _isDueSoonMap.value[hw.title]
                        if (existing == null || existing is ApiResponse.Error) {
                            checkIsDueSoon(hw.title, hw.deadline)
                        }
                    }
                }
            }
            catch (e: Exception){
                Log.e(TAG,"failed to get Homeworks:${e}")
                // Update the map with error for this specific course
                val updatedMap = _pendingHomeworksByCourse.value.toMutableMap()
                updatedMap[courseId] = ApiResponse.Error(e.message ?: "Unknown error")
                _pendingHomeworksByCourse.value = updatedMap
            }

        }
    }
    
    fun handleCourseClick(courseId: String) {
        val isCurrentlyExpanded = isCourseExpanded(courseId)
        toggleCourseExpanded(courseId)
        
        // 只有在展开时才加载数据
        if (!isCurrentlyExpanded) {
            val isCoursePreloaded = isCoursePreloaded(courseId)
            if (!isCoursePreloaded) {
                markCourseAsPreloaded(courseId)
                getCourseHomeworks(courseId)
            } else {
                // 如果已经预加载过，直接刷新数据
                getCourseHomeworks(courseId)
            }
        }
    }
    fun checkIsDueSoon(homeworkId: String, deadline: String) {
        viewModelScope.launch {
            // set loading for this homework
            _isDueSoonMap.value = _isDueSoonMap.value.toMutableMap().apply {
                this[homeworkId] = ApiResponse.Loading()
            }

            try {
                val res = repository.isHomeworkDueWithinOneDay(deadline)
                    .filter { it !is ApiResponse.Loading }
                    .first()
                val updated = _isDueSoonMap.value.toMutableMap()
                updated[homeworkId] = res
                _isDueSoonMap.value = updated
            } catch (e: Exception) {
                Log.e(TAG, "checkIsDueSoon failed: ${e.message}", e)
                val updated = _isDueSoonMap.value.toMutableMap()
                updated[homeworkId] = ApiResponse.Error(e.message ?: "计算截止时间失败")
                _isDueSoonMap.value = updated
            }
        }
    }
}