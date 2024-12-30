package com.example.changli_planet_app.Cache

import com.tencent.mmkv.MMKV

object UserInfoManager {
    private val mmkv by lazy { MMKV.defaultMMKV() }

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

    var token: String
        get() = mmkv.getString(KEY_TOKEN, "") ?: ""
        set(value) {
            mmkv.putString(KEY_TOKEN, value)
        }
    fun clear() {
        username = ""
        userPassword = ""
        token = ""
    }
}