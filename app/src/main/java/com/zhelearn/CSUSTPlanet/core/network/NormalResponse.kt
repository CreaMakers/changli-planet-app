package com.zhelearn.CSUSTPlanet.core.network

import androidx.annotation.Keep

@Keep
data class NormalResponse(
    val code: String, val msg: String
)