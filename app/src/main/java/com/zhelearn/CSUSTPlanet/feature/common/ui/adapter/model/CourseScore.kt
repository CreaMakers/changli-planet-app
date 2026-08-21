package com.zhelearn.CSUSTPlanet.feature.common.ui.adapter.model

import androidx.annotation.Keep

@Keep
data class CourseScore(
    val name: String,
    val score: Int,
    val credit: Double,
    val earnedCredit: Double,
    val courseType: String,
    val pscjUrl: String?
)