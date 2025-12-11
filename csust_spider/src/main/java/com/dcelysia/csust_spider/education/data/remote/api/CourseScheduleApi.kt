package com.dcelysia.csust_spider.education.data.remote.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CourseScheduleApi {
    @FormUrlEncoded
    @POST("/jsxsd/xskb/xskb_list.do")
    suspend fun getCourseSchedule(
        @Field("zc")week: String,
        @Field("xnxq01id") academicYearSemester: String = ""): Response<String>
}