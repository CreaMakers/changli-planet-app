package com.example.changli_planet_app.Network.Response

data class Pagination(
    val current_page: Int,
    val page_size: Int,
    val total_pages: Int,
    val total_posts: Int
)