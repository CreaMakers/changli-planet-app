package com.example.changli_planet_app.Network

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType
import java.io.File

class HttpUrlHelper private constructor(
    private val baseUrl: String,
    private val pathParams: Map<String, String>,
    private val queryParams: Map<String, String>,
    val headers: Map<String, String>,
    val requestType: RequestType,
    val requestBody: String? = null,
    val formParams: MutableMap<String, String>,
    val fileParams: MutableMap<String, Pair<File, MediaType?>>
) {
    enum class RequestType {
        GET, POST, PUT, DELETE
    }

    class HttpRequest() {
        private var url: String? = null
        private val pathParams: MutableMap<String, String> = mutableMapOf()
        private val queryParams: MutableMap<String, String> = mutableMapOf()
        private val headers: MutableMap<String, String> = mutableMapOf()
        private var requestBody: String? = null
        private var requestType: RequestType = RequestType.GET
        private val formParams: MutableMap<String, String> = mutableMapOf()
        private val fileParams: MutableMap<String, Pair<File, MediaType?>> = mutableMapOf()
        fun get(baseurl: String): HttpRequest {
            url = baseurl
            requestType = RequestType.GET
            return this
        }

        fun post(baseurl: String): HttpRequest {
            url = baseurl
            requestType = RequestType.POST
            return this
        }

        fun delete(baseurl: String): HttpRequest {
            url = baseurl
            requestType = RequestType.DELETE
            return this
        }

        fun put(baseurl: String): HttpRequest {
            url = baseurl
            requestType = RequestType.PUT
            return this
        }

        fun addFormField(key: String, value: String): HttpRequest {
            formParams[key] = value
            return this
        }

        fun addFieldPart(key: String, file: File, mediaType: MediaType? = null): HttpRequest {
            fileParams[key] = Pair(file, mediaType)
            return this
        }

        fun header(key: String, value: String): HttpRequest {
            headers[key] = value
            return this
        }

        fun addPathParam(key: String, value: String): HttpRequest {
            pathParams[key] = value
            return this
        }

        fun addQueryParam(key: String, value: String): HttpRequest {
            queryParams[key] = value
            return this
        }

        fun body(json: String?): HttpRequest {
            requestBody = json
            return this
        }

        fun build(): HttpUrlHelper {
            return HttpUrlHelper(
                url ?: throw IllegalArgumentException("Base URL must not be null"),
                pathParams,
                queryParams,
                headers,
                requestType,
                requestBody,
                formParams,
                fileParams
            )
        }
    }

    fun buildUrl(): String {
        var finalUrl = baseUrl
        for ((key, value) in pathParams) {
            finalUrl = finalUrl.replace("{$key}", value)
        }
        // 添加查询参数
        val httpUrlBuilder = finalUrl.let { it.toHttpUrlOrNull()?.newBuilder() }
        for ((key, value) in queryParams) {
            httpUrlBuilder?.addQueryParameter(key, value)
        }
        return httpUrlBuilder?.build().toString()
    }
}