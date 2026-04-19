package com.creamaker.changli_planet_app.common.data.local.mmkv

import com.creamaker.changli_planet_app.common.data.local.kv.MigratingKv
import com.tencent.mmkv.MMKV

object StudentInfoManager {
    private const val CACHE_ID = "stu_info_cache"
    private const val LEGACY_CACHE_ID = "import_cache"

    private const val KEY_STUDENT_ID = "student_id"
    private const val KEY_PASSWORD = "student_password"

    private val kv by lazy {
        MigratingKv(CACHE_ID) {
            listOf(
                MMKV.mmkvWithID(CACHE_ID),
                MMKV.mmkvWithID(LEGACY_CACHE_ID)
            )
        }
    }

    var studentId: String
        get() = kv.getString(KEY_STUDENT_ID, "") ?: ""
        set(value) {
            kv.putString(KEY_STUDENT_ID, value)
        }

    var studentPassword: String
        get() = kv.getString(KEY_PASSWORD, "") ?: ""
        set(value) {
            kv.putString(KEY_PASSWORD, value)
        }

    fun clear() {
        studentId = ""
        studentPassword = ""
    }
}