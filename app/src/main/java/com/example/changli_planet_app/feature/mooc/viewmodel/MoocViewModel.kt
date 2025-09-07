package com.example.changli_planet_app.feature.mooc.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.changli_planet_app.R
import com.example.changli_planet_app.core.network.Resource
import com.example.changli_planet_app.feature.mooc.data.remote.dto.PendingAssignmentCourse
import com.example.changli_planet_app.feature.mooc.data.remote.repository.MoocRepository
import com.example.changli_planet_app.utils.ResourceUtil
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
                    val courseResult = repository.getCourseNamesWithPendingHomeworks().first()
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
}