package com.zhelearn.CSUSTPlanet.core.network

import androidx.annotation.Keep

@Keep
data class MyResponse<T>(val code: String, val data: T, val msg: String)