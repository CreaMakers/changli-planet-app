package com.example.changli_planet_app

import com.example.changli_planet_app.Network.Response.Course

data class CoursesResponse(val code : String , val msg : String ,val data: List<Course> )
