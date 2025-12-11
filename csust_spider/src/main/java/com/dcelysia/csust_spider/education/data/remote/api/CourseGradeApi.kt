package com.dcelysia.csust_spider.education.data.remote.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface CourseGradeApi {

    @GET("/jsxsd/kscj/cjcx_query")
    suspend fun getCourseGradePage(): Response<String>

    @FormUrlEncoded
    @POST("/jsxsd/kscj/cjcx_list")
    suspend fun getCourseGrades(
        @Field("kksj") semester: String,
        @Field("kcxz") courseNature: String,
        @Field("kcmc") courseName: String,
        @Field("xsfs") displayMode: String,
        @Field("fxkc") studyMode: String
    ): Response<String>
    @GET
    suspend fun getGradeDetail(
        @Url url: String
    ): Response<String>

}