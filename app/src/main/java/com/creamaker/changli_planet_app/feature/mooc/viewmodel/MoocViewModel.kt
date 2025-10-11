package com.creamaker.changli_planet_app.feature.mooc.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.core.network.Resource
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

    private val _isSuccessLogin = MutableStateFlow<Resource<Boolean>>(Resource.Loading())
    val isSuccessLogin = _isSuccessLogin.asStateFlow()

    private val _pendingCourse =
        MutableStateFlow<Resource<List<PendingAssignmentCourse>>>(Resource.Loading())
    val pendingCourse = _pendingCourse.asStateFlow()
    private val _pendingHomeworksByCourse = MutableStateFlow<Map<String, Resource<List<MoocHomework>>>>(mapOf())
    val pendingHomeworksByCourse = _pendingHomeworksByCourse.asStateFlow()
    
    private val _expandedCourseIds = MutableStateFlow<Set<String>>(setOf())
    val expandedCourseIds = _expandedCourseIds.asStateFlow()
    
    private val _preloadedCourseIds = MutableStateFlow<Set<String>>(setOf())
    val preloadedCourseIds = _preloadedCourseIds.asStateFlow()
    // 新增：每个作业是否一天内到期的状态 map（key = homeworkId）
    private val _isDueSoonMap = MutableStateFlow<Map<String, Resource<Boolean>>>(mapOf())
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
                        Resource.Error(ResourceUtil.getStringRes(R.string.school_account_not_bound_cannot_query))
                }
                _isSuccessLogin.value = Resource.Loading()
                _pendingCourse.value = Resource.Loading()
                val loginResult = repository.login(account, password)
                    .filter { it !is Resource.Loading }
                    .first()
                _isSuccessLogin.value = loginResult

                if (loginResult is Resource.Success && loginResult.data) {
                    val courseResult = repository.getCourseNamesWithPendingHomeworks()
                        .filter { it !is Resource.Loading }
                        .first()
                    Log.d(TAG,courseResult.toString())
                    _pendingCourse.value = courseResult
                } else {
                    _pendingCourse.value = Resource.Error((loginResult as Resource.Error).msg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login and fetch courses failed", e)
                _isSuccessLogin.value =
                    Resource.Error(e.message ?: ResourceUtil.getStringRes(R.string.error_unknown))
                _pendingCourse.value =
                    Resource.Error(e.message ?: ResourceUtil.getStringRes(R.string.error_unknown))
            }
        }
    }
    fun getCourseHomeworks(courseId: String){
        viewModelScope.launch {
            try {
                // Set loading state for this specific course
                val currentMap = _pendingHomeworksByCourse.value
                _pendingHomeworksByCourse.value = currentMap.toMutableMap().apply {
                    this[courseId] = Resource.Loading()
                }

                val result = repository.getCourseHomeworks(courseId)
                    .filter { it !is Resource.Loading }
                    .first()

                // Update the map with the result for this specific course
                val updatedMap = _pendingHomeworksByCourse.value.toMutableMap()
                updatedMap[courseId] = result
                _pendingHomeworksByCourse.value = updatedMap

                // 如果获取成功，则为每个作业触发 isDueSoon 检查并更新 ViewModel 中的状态
                if (result is Resource.Success) {
                    result.data.forEach { hw ->
                        // 只有在还没有检查或者之前是错误/已过时情况下才重新检查
                        val existing = _isDueSoonMap.value[hw.title]
                        if (existing == null || existing is Resource.Error) {
                            checkIsDueSoon(hw.title, hw.deadline)
                        }
                    }
                }
            }
            catch (e: Exception){
                Log.e(TAG,"failed to get Homeworks:${e}")
                // Update the map with error for this specific course
                val updatedMap = _pendingHomeworksByCourse.value.toMutableMap()
                updatedMap[courseId] = Resource.Error(e.message ?: "Unknown error")
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
                this[homeworkId] = Resource.Loading()
            }

            try {
                val res = repository.isHomeworkDueWithinOneDay(deadline)
                    .filter { it !is Resource.Loading }
                    .first()
                val updated = _isDueSoonMap.value.toMutableMap()
                updated[homeworkId] = res
                _isDueSoonMap.value = updated
            } catch (e: Exception) {
                Log.e(TAG, "checkIsDueSoon failed: ${e.message}", e)
                val updated = _isDueSoonMap.value.toMutableMap()
                updated[homeworkId] = Resource.Error(e.message ?: "计算截止时间失败")
                _isDueSoonMap.value = updated
            }
        }
    }
}