package com.dcelysia.csust_spider.education.data.remote.api


import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ExamApi {


    @FormUrlEncoded
    @POST("/jsxsd/xsks/xsksap_list")
    suspend fun queryExamList(
        @Field("xqlbmc")semesterType :String,
        @Field("xnxqid")queryAcadmicYearSemester: String,
        @Field("xqlb")semesterid: String
    ): Response<String>

    @FormUrlEncoded
    @GET("/jsxsd/xsks/xsksap_query")
    suspend fun getExamSemester(): Response<String>

}

