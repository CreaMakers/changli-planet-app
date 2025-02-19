package com.example.changli_planet_app.Cache

import com.example.changli_planet_app.Core.PlanetApplication
import com.tencent.mmkv.MMKV

object UserInfoManager {
    private val mmkv by lazy { MMKV.mmkvWithID("import_cache") }

    private const val KEY_USERNAME = "account"
    private const val KEY_USER_PASSWORD = "user_password"
    private const val KEY_TOKEN = "user_token"

    var username: String
        get() = mmkv.getString(KEY_USERNAME, "") ?: ""
        set(value) {
            mmkv.putString(KEY_USERNAME, value)
        }

    var userPassword: String
        get() = mmkv.getString(KEY_USER_PASSWORD, "") ?: ""
        set(value) {
            mmkv.putString(KEY_USER_PASSWORD, value)
        }

    fun clear() {
        username = ""
        userPassword = ""
        PlanetApplication.accessToken = null
    }
}