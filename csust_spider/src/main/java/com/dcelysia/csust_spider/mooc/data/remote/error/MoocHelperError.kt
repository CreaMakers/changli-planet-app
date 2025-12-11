package com.dcelysia.csust_spider.mooc.data.remote.error

sealed class MoocHelperError(message: String) : Exception(message) {
    class ProfileRetrievalFailed(message: String) :
        MoocHelperError("Profile retrieval failed: $message")

    class CourseRetrievalFailed(message: String) :
        MoocHelperError("Course retrieval failed: $message")

    class TestRetrievalFailed(message: String) : MoocHelperError("Test retrieval failed: $message")
    class CourseNamesWithPendingHomeworksRetrievalFailed(message: String) :
        MoocHelperError("Course names with pending homeworks retrieval failed: $message")

    class CookieExpiredException(message: String) : Exception(message)
}