package com.dcelysia.csust_spider.core

sealed class Resource<T> {
    data class Success<T> (val data: T) : Resource<T>()
    data class Error<T> (val msg: String) : Resource<T>()
    class Loading<T> : Resource<T>()
}