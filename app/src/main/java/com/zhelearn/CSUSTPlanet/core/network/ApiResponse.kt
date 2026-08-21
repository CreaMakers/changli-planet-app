package com.zhelearn.CSUSTPlanet.core.network

import androidx.annotation.Keep

@Keep
sealed class ApiResponse<T> {
    data class Success<T> (val data: T) : ApiResponse<T>()
    data class Error<T> (val msg: String) : ApiResponse<T>()
    class Loading<T> : ApiResponse<T>()
}