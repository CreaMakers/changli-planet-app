package com.creamaker.changli_planet_app.common.data.local.mmkv

import com.tencent.mmkv.MMKV

object StudentInfoManager {
    private val mmkv by lazy {
        MMKV.mmkvWithID(CACHE_ID).also { target ->
            migrateFromLegacyIfNeeded(target)
        }
    }

    private const val CACHE_ID = "stu_info_cache"
    private const val LEGACY_CACHE_ID = "import_cache"

    private const val KEY_STUDENT_ID = "student_id"
    private const val KEY_PASSWORD = "student_password"

    private fun migrateFromLegacyIfNeeded(target: MMKV) {
        val hasCurrentData =
            !target.getString(KEY_STUDENT_ID, "").isNullOrEmpty() ||
                !target.getString(KEY_PASSWORD, "").isNullOrEmpty()
        if (hasCurrentData) return

        val legacy = MMKV.mmkvWithID(LEGACY_CACHE_ID)
        val legacyStudentId = legacy.getString(KEY_STUDENT_ID, "") ?: ""
        val legacyPassword = legacy.getString(KEY_PASSWORD, "") ?: ""
        if (legacyStudentId.isEmpty() && legacyPassword.isEmpty()) return

        target.putString(KEY_STUDENT_ID, legacyStudentId)
        target.putString(KEY_PASSWORD, legacyPassword)
    }

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