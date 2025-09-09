package com.example.changli_planet_app.feature.mooc.data.remote.api

import com.example.changli_planet_app.feature.mooc.data.remote.dto.MoocHomeworkResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MoocApi {
    @GET("/meol/homepage/common/sso_login.jsp")
    suspend fun loginToMooc(): Response<String>

    @GET("/meol/personal.do")
    suspend fun getProfile(): Response<String>

    @GET("/meol/lesson/blen.student.lesson.list.jsp")
    suspend fun getCourses(): Response<String>

    @GET("/meol/hw/stu/hwStuHwtList.do")
    suspend fun getCourseHomeworks(
        @Query("sortDirection") sortDirection: Int = -1,
        @Query("courseId") courseId: String,
        @Query("pagingPage") pagingPage: Int = 1,
        @Query("pagingNumberPer") pagingNumberPer: Int = 1000,
        @Query("sortColumn") sortColumn: String = "deadline"
    ): Response<MoocHomeworkResponse>

    @GET("/meol/common/question/test/student/list.jsp")
    suspend fun getCourseTests(
        @Query("sortColumn") sortColumn: String = "createTime",
        @Query("sortDirection") sortDirection: Int = -1,
        @Query("cateId") cateId: String,
        @Query("pagingPage") pagingPage: Int = 1,
        @Query("status") status: Int = 1,
        @Query("pagingNumberPer") pagingNumberPer: Int = 1000
    ): Response<String>

    @GET("/meol/welcomepage/student/interaction_reminder_v8.jsp")
    suspend fun getCourseNamesWithPendingHomeworks(): Response<String>

    @GET("/meol/homepage/V8/include/logout.jsp")
    suspend fun logout(): Response<String>
}