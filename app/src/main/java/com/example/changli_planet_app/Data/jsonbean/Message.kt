package com.example.changli_planet_app.Data.jsonbean

import java.io.File

data class Message(
    val message_content:String,
    val file: File ?= null,
    val receiver_id: Int ?= null
)
