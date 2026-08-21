package com.zhelearn.CSUSTPlanet.feature.common.data.remote.dto

import androidx.annotation.Keep

@Keep
data class CheckElectricity(
    val address: String,
    val buildId: String,
    val nod: String
)