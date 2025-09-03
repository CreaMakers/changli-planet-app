package com.example.changli_planet_app.FreshNews.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.changli_planet_app.Network.Resource
import com.example.changli_planet_app.Network.Response.UserProfile
import com.example.changli_planet_app.Network.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserHomeViewModel : ViewModel() {

    private val userProfileCase by lazy { UserProfileRepository.Companion.instance }

    private val _userProfile = MutableStateFlow<Resource<UserProfile>>(Resource.Loading())
    val userProfile = _userProfile.asStateFlow()

    fun getUserProfile(userId: Int) {
        viewModelScope.launch {
            userProfileCase.getUserInFormationNoCache(userId).collect { result ->
                _userProfile.value = result
            }
        }
    }

}