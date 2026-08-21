package com.zhelearn.CSUSTPlanet.feature.calendar.data.remote.api

import com.zhelearn.CSUSTPlanet.feature.calendar.data.remote.dto.SemesterCalendarDetail
import com.zhelearn.CSUSTPlanet.feature.calendar.data.remote.dto.SemesterCalendarListItem
import retrofit2.http.GET
import retrofit2.http.Path

interface SemesterCalendarApi {

    @GET("config/semester-calendars")
    suspend fun getSemesterCalendars(): List<SemesterCalendarListItem>

    @GET("config/semester-calendars/{semester_code}")
    suspend fun getSemesterCalendarDetail(
        @Path("semester_code") semesterCode: String
    ): SemesterCalendarDetail
}
