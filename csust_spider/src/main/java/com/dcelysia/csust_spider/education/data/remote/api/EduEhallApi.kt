package com.dcelysia.csust_spider.education.data.remote.api

import retrofit2.Response
import retrofit2.http.POST

interface EduEhallApi {

    @POST("/Logon.do?method=logon&flag=sess")
    suspend fun getEhall(): Response<String>



}