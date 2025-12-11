package com.dcelysia.csust_spider.education.data.remote.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

//interface RelexClassroomInfoApi {
//    /**
//     * 提交教室查询表单并返回原始 HTML 响应体。
//     *
//     * @param xnxqh 学期字段，示例："2024-2025-1"
//     * @param xqbh 校区编号，用于指定查询的校区/区域
//     * @param zc 开始周次（查询的起始周）
//     * @param zc2 结束周次（查询的结束周）示例：第一周
//     * @param xq 开始星期，示例：星期一
//     * @param xq2 结束星期，示例：星期二
//     * @param jc 开始节次（节次的起始值）示例：01
//     * @param jc2 结束节次（节次的结束值）示例：02
//     */
//    @FormUrlEncoded
//    @POST("/jsxsd/kbxx/jsjyjl_list")
//    suspend fun getRelexClassroom(
//        @Field("xnxqh") xnxqh: String,
//        @Field("typewhere") typewhere: String = "jszq",
//        @Field("xqbh") xqbh: String,
//        @Field("jszt") jszt: String = "8",
//        @Field("zc") zc: String,
//        @Field("zc2") zc2: String,
//        @Field("xq") xq: String,
//        @Field("xq2") xq2: String,
//        @Field("jc") jc: String,
//        @Field("jc2") jc2: String
//    ): Response<String>
//}