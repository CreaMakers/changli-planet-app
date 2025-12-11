package com.dcelysia.csust_spider.education.data.remote

import com.tencent.mmkv.MMKV

object EducationData {
    private val mmkv by lazy { MMKV.mmkvWithID("education_cache") }
    private const val KEY_STUDENT_ID = "student_id"
    private const val KEY_PASSWORD = "student_password"

    var studentId: String
        get() = mmkv.getString(KEY_STUDENT_ID, "") ?: ""
        set(value) {
            mmkv.putString(KEY_STUDENT_ID, value)
        }

    var studentPassword: String
        get() = mmkv.getString(KEY_PASSWORD, "") ?: ""
        set(value) {
            mmkv.putString(KEY_PASSWORD, value)
        }

    fun clear() {
        studentId = ""
        studentPassword = ""
    }
}