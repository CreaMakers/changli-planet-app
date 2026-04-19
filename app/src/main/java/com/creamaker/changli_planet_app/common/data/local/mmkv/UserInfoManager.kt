package com.creamaker.changli_planet_app.common.data.local.mmkv

import com.creamaker.changli_planet_app.common.data.local.kv.MigratingKv
import com.creamaker.changli_planet_app.core.PlanetApplication

object UserInfoManager {
    private val kv by lazy { MigratingKv("import_cache") }

    private const val KEY_USERID = "user_id"
    private const val KEY_USERNAME = "account"
    private const val KEY_USER_PASSWORD = "user_password"
    private const val KEY_AVATAR = "user_avatar"

    private const val KEY_USER_ACCOUNT = "user_account"
    private const val KEY_EMAIL = "user_email"

    var userId: Int
        get() = kv.getInt(KEY_USERID, -1)
        set(value) {
            kv.putInt(KEY_USERID, value)
        }

    var username: String
        get() = kv.getString(KEY_USERNAME, "") ?: ""
        set(value) {
            kv.putString(KEY_USERNAME, value)
        }

    var account: String
        get() = kv.getString(KEY_USER_ACCOUNT, "") ?: ""
        set(value) {
            kv.putString(KEY_USER_ACCOUNT, value)
        }

    var userPassword: String
        get() = kv.getString(KEY_USER_PASSWORD, "") ?: ""
        set(value) {
            kv.putString(KEY_USER_PASSWORD, value)
        }

    var userAvatar: String
        get() = kv.getString(KEY_AVATAR, "https://pic.imgdb.cn/item/671e5e17d29ded1a8c5e0dbe.jpg")
            ?: "https://pic.imgdb.cn/item/671e5e17d29ded1a8c5e0dbe.jpg"
        set(value) {
            kv.putString(KEY_AVATAR, value)
        }

    var userEmail: String
        get() = kv.getString(KEY_EMAIL, "") ?: ""
        set(value) {
            kv.putString(KEY_EMAIL, value)
        }

    fun clear() {
        username = ""
        userPassword = ""
        PlanetApplication.accessToken = null
    }
}