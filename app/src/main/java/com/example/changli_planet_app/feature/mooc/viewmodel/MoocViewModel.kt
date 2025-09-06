package com.example.changli_planet_app.feature.mooc.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.changli_planet_app.feature.mooc.data.remote.repository.MoocRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MoocViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "MoocViewModel"
    }

    private val repository by lazy { MoocRepository.instance }

    fun login(account: String, password: String) {
        viewModelScope.launch {
            val loginResult = repository.login(account, password)
            loginResult.collect {

            }
            val profileResult = repository.getProfile()
            profileResult.onEach { profileResult ->
                val result = profileResult
                Log.d(TAG, "$result")
            }.launchIn(viewModelScope)
            val test = repository.getCourseNamesWithPendingHomeworks()
            test.collect {
                Log.d(TAG, "test $it")
            }
        }
    }
}