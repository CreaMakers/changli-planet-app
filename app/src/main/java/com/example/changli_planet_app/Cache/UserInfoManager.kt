package com.example.changli_planet_app.Cache

import com.example.changli_planet_app.Core.PlanetApplication
import com.tencent.mmkv.MMKV

object UserInfoManager {
    private val mmkv by lazy { MMKV.mmkvWithID("import_cache") }

    private const val KEY_USERID="user_id"
    private const val KEY_USERNAME = "account"
    private const val KEY_USER_PASSWORD = "user_password"
    private const val KEY_AVATAR = "user_avatar"

    var userId:Int
        get() = mmkv.getInt(KEY_USERID,-1)
        set(value){
            mmkv.putInt(KEY_USERID,value)
        }

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

    var userAvatar: String
        get() = mmkv.getString(KEY_AVATAR, "https://pic.imgdb.cn/item/671e5e17d29ded1a8c5e0dbe.jpg")
            ?: "https://pic.imgdb.cn/item/671e5e17d29ded1a8c5e0dbe.jpg"
        set(value) {
            mmkv.putString(KEY_AVATAR, value)
        }

    fun clear() {
        username = ""
        userPassword = ""
        PlanetApplication.accessToken = null
    }
}