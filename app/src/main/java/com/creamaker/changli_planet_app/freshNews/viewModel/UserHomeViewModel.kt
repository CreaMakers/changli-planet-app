package com.creamaker.changli_planet_app.freshNews.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.common.data.remote.dto.UserProfile
import com.creamaker.changli_planet_app.core.network.ApiResponse
import com.creamaker.changli_planet_app.freshNews.data.remote.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserHomeViewModel : ViewModel() {

    private val userProfileCase by lazy { UserProfileRepository.Companion.instance }

    private val _userProfile = MutableStateFlow<ApiResponse<UserProfile>>(ApiResponse.Loading())
    val userProfile = _userProfile.asStateFlow()

    fun getUserProfile(userId: Int) {
        viewModelScope.launch {
            userProfileCase.getUserInFormationNoCache(userId).collect { result ->
                _userProfile.value = result
            }
        }
    }

}